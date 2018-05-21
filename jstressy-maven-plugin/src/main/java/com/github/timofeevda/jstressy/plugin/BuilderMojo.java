/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Denis Timofeev <timofeevda@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *
 */

package com.github.timofeevda.jstressy.plugin;

import com.github.timofeevda.jstressy.plugin.api.Exclusion;
import com.github.timofeevda.jstressy.plugin.api.ScenarioBundle;
import com.github.timofeevda.jstressy.plugin.api.SystemBundle;
import com.github.timofeevda.jstressy.plugin.core.ArtifactFileNameUtils;
import com.github.timofeevda.jstressy.plugin.core.ArtifactResolver;
import com.github.timofeevda.jstressy.plugin.core.Constants;
import com.github.timofeevda.jstressy.plugin.core.DependencyDescriptor;
import com.github.timofeevda.jstressy.plugin.core.FelixConfigWriter;
import com.github.timofeevda.jstressy.plugin.core.FileUtils;
import com.github.timofeevda.jstressy.plugin.core.JarUtils;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mojo(name = "build", aggregator = true)
public class BuilderMojo extends AbstractMojo {

    @Parameter(defaultValue = "zip")
    private String archive;

    @Parameter(defaultValue = "5.6.10")
    private String felixVersion;

    @Parameter(defaultValue = "")
    private String felixConfigFile;

    @Parameter(defaultValue = "")
    private String logbackFile;

    @Parameter(defaultValue = "")
    private String runBatFile;

    @Parameter(defaultValue = "")
    private String runShFile;

    @Parameter(defaultValue = "")
    private String stressyConfigFile;

    @Parameter
    private List<Exclusion> exclusions = Collections.emptyList();

    @Parameter
    private List<SystemBundle> systemBundles = Collections.emptyList();

    @Parameter
    private List<ScenarioBundle> scenarioBundles = Collections.emptyList();

    @Parameter(defaultValue = "target")
    private String target;

    @Parameter(defaultValue = "${project}")
    private MavenProject mavenProject;

    @Component
    private RepositorySystem repositorySystem;

    @Parameter(defaultValue = "${repositorySystemSession}")
    private RepositorySystemSession repositorySystemSession;

    @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true)
    private List<RemoteRepository> remoteRepositories;

    private ArtifactResolver artifactResolver;

    @Override
    public void execute() throws MojoFailureException {
        artifactResolver = new ArtifactResolver(remoteRepositories,
                repositorySystem, repositorySystemSession, exclusions, getLog());

        deleteOutputFolder();

        createOutputFolders();

        copyConfigurationFiles();

        // get list of system bundles - bundles needed to be run before all other bundles, right after felix start
        Collection<DependencyDescriptor> systemBundles = getSystemBundles()
                .stream()
                .map(DependencyDescriptor::deriveAetherArtifact)
                .collect(Collectors.toSet());

        // get list of scenario bundles -
        Collection<DependencyDescriptor> scenarioBundles = getScenarioBundles()
                .stream().map(DependencyDescriptor::deriveAetherArtifact)
                .collect(Collectors.toSet());

        // prepare list of bundles to exclude from application folder
        Set<DependencyDescriptor> exclusionsSet =
                Stream.concat(systemBundles.stream(), scenarioBundles.stream())
                        .map(descriptor -> artifactResolver.resolveArtifact(
                                descriptor.getGroupId(), descriptor.getArtifactId(), descriptor.getClassifier(),
                                descriptor.getType(), descriptor.getVersion()
                        ))
                        .map(DependencyDescriptor::deriveAetherArtifact)
                        .collect(Collectors.toSet());

        Collection<DependencyDescriptor> applicationBundles = getApplicationBundles(exclusionsSet)
                .stream()
                .map(DependencyDescriptor::deriveAetherArtifact)
                .collect(Collectors.toSet());

        copySystemBundlesArtifacts(getBundlesWithMaxVersions(systemBundles));
        copyApplicationBundlesArtifact(getBundlesWithMaxVersions(applicationBundles));
        copyScenarioBundles(getBundlesWithMaxVersions(scenarioBundles));

        writeFelixConfig(systemBundles);

        writeFelixRunnable();

        archiveBundle();
    }

    private void deleteOutputFolder() {
        FileUtils.deleteFolder(new File(target));
    }

    private Collection<DependencyDescriptor> getBundlesWithMaxVersions(Collection<DependencyDescriptor> dependencyDescriptors) {
        return dependencyDescriptors.stream()
                .collect(Collectors.toMap(
                        dd -> dd.getGroupId() + dd.getArtifactId(),
                        Function.identity(),
                        this::selectMaxVersionDependency))
                .values();
    }

    private void writeFelixRunnable() throws MojoFailureException {
        copyArtifactFile(DependencyDescriptor.deriveAetherArtifact(downloadFelixBundle()), "bin/", "felix.jar");
    }

    private void writeFelixConfig(Collection<DependencyDescriptor> systemBundles) {
        try {
            // get list of system bundles we need to start automatically before the start of all framework
            // (e.g logging bundle which needs to be initialized first)
            List<String> listOfAutoStartBundles = systemBundles
                    .stream()
                    .map(dd -> Constants.BUNDLES_SYSTEM + ArtifactFileNameUtils.getArtifactFileName(dd.getAetherArtifact())).collect(Collectors.toList());
            FelixConfigWriter.writeConfig(target, target, listOfAutoStartBundles);
        } catch (IOException e) {
            getLog().error(e);
        }
    }

    private void createOutputFolders() {
        try {
            FileUtils.createFolders(new File(target + File.separator + Constants.FELIX_BIN),
                    new File(target + File.separator + Constants.BUNDLES_APPLICATION),
                    new File(target + File.separator + Constants.BUNDLES_SYSTEM),
                    new File(target + File.separator + Constants.BUNDLES_PLUGIN),
                    new File(target + Constants.CONFIGURATION_FOLDER));
        } catch (IOException e) {
            getLog().error(e);
        }
    }

    private Artifact downloadFelixBundle() throws MojoFailureException {
        Artifact artifact = new DefaultArtifact(Constants.FELIX_GROUP_ID, Constants.FELIX_ARTIFACT_ID,
                "jar", felixVersion);

        ArtifactRequest artifactRequest = new ArtifactRequest();
        artifactRequest.setRepositories(remoteRepositories);
        artifactRequest.setArtifact(artifact);

        ArtifactResult artifactResult;
        try {
            artifactResult = repositorySystem.resolveArtifact(repositorySystemSession, artifactRequest);
        } catch (org.eclipse.aether.resolution.ArtifactResolutionException ex) {
            getLog().error(ex);
            throw new MojoFailureException("Couldn't get Apache Felix runnable");
        }

        File artifactFile = new File(
                repositorySystemSession.getLocalRepository().getBasedir()
                        + File.separator
                        + repositorySystemSession.getLocalRepositoryManager().getPathForLocalArtifact(artifactRequest.getArtifact()));
        return artifactResult.getArtifact().setFile(artifactFile);
    }

    private void copyApplicationBundlesArtifact(Collection<DependencyDescriptor> dependencyList) {
        dependencyList.forEach(descriptor -> copyArtifactFile(descriptor, Constants.BUNDLES_APPLICATION));
    }

    private void copySystemBundlesArtifacts(Collection<DependencyDescriptor> dependencyList) {
        dependencyList.forEach(descriptor -> copyArtifactFile(descriptor, Constants.BUNDLES_SYSTEM));
    }

    private void copyScenarioBundles(Collection<DependencyDescriptor> scenarioBundles) {
        scenarioBundles.forEach(bundle -> copyArtifactFile(bundle, Constants.BUNDLES_PLUGIN));
    }

    private void copyArtifactFile(DependencyDescriptor dependencyDescriptor, String folder) {
        copyArtifactFile(dependencyDescriptor, folder, null);
    }

    private void copyArtifactFile(DependencyDescriptor dependencyDescriptor, String folder, String fileNameOverride) {
        String artifactFileName =
                fileNameOverride != null ? fileNameOverride :
                        ArtifactFileNameUtils.getArtifactFileName(dependencyDescriptor);
        try {
            if (!JarUtils.isBundle(new File(dependencyDescriptor.getPath()))) {
                getLog().warn("Skipping non-bundle artifact: " + artifactFileName);
                return;
            }
        } catch (Exception ex) {
            getLog().warn(ex);
        }

        try {
            File fileToCopy = new File(target + File.separator + folder + artifactFileName);
            if (!fileToCopy.exists()) {
                FileUtils.copyFile(new File(dependencyDescriptor.getPath()), fileToCopy);
            }
        } catch (IOException e) {
            getLog().error(e);
        }
    }

    private void copyConfigurationFiles() {
        try {
            writeFelixConfig();
            writeLogbackConfig();
            writeWindowsRunner();
            writeUnixRunner();
            writeStressyConfig();
        } catch (Exception ex) {
            getLog().error(ex);
        }
    }

    private void writeStressyConfig() throws Exception {
        InputStream templateConfig = stressyConfigFile == null ?
                BuilderMojo.class.getResourceAsStream("/stressy/stressy.yml") :
                new FileInputStream(stressyConfigFile);
        FileUtils.copyFile(templateConfig, new File(target + "/stressy.yml"));
    }

    private void writeUnixRunner() throws Exception {
        InputStream unixRunner = runShFile == null ?
                BuilderMojo.class.getResourceAsStream("/run/run.sh") :
                new FileInputStream(runShFile);
        FileUtils.copyFile(unixRunner, new File(target + "/run.sh"));
    }

    private void writeWindowsRunner() throws Exception {
        InputStream windowsRunner = runBatFile == null ?
                BuilderMojo.class.getResourceAsStream("/run/run.bat") :
                new FileInputStream(runBatFile);
        FileUtils.copyFile(windowsRunner, new File(target + "/run.bat"));
    }

    private void writeLogbackConfig() throws Exception {
        // copy logback config
        InputStream logback = logbackFile == null ?
                BuilderMojo.class.getResourceAsStream("/logback/logback.xml") :
                new FileInputStream(logbackFile);
        FileUtils.copyFile(logback, new File(target + Constants.CONFIGURATION_FOLDER + "/logback.xml"));
    }

    private void writeFelixConfig() throws Exception {
        InputStream felixConfig = felixConfigFile == null ?
                BuilderMojo.class.getResourceAsStream("/felix/" + Constants.FELIX_CONFIG) :
                new FileInputStream(felixConfigFile);
        FileUtils.copyFile(felixConfig, new File(target + Constants.CONFIGURATION_FOLDER + "/" + Constants.FELIX_CONFIG));
    }

    private Set<Artifact> getApplicationBundles(Set<DependencyDescriptor> exclusionsSet) {
        return mavenProject.getDependencies()
                .stream()
                .flatMap(dep -> getBundleDependencies(dep).stream())
                .distinct()
                .filter(artifact -> !exclusionsSet.contains(DependencyDescriptor.deriveAetherArtifact(artifact)))
                .collect(Collectors.toSet());
    }

    private Collection<Artifact> getSystemBundles() {
        return systemBundles == null ? Collections.emptySet() :
                systemBundles.stream()
                        .flatMap(systemBundle ->
                                mavenProject.getDependencies().stream()
                                        .filter(dependency -> systemBundle.getArtifactId().equals(dependency.getArtifactId())
                                                && systemBundle.getGroupId().equals(dependency.getGroupId())))
                        .flatMap(dep -> getBundleDependencies(dep).stream())
                        .collect(Collectors.toSet());
    }

    private Set<Artifact> getScenarioBundles() {
        return scenarioBundles == null ? Collections.emptySet()
                : scenarioBundles.stream()
                .flatMap(scenarioBundle -> mavenProject.getDependencies().stream()
                        .filter(dependency -> scenarioBundle.getArtifactId().equals(dependency.getArtifactId())
                                && scenarioBundle.getGroupId().equals(dependency.getGroupId())))
                .map(DependencyDescriptor::deriveMavenDependency)
                .map(dd -> artifactResolver.resolveArtifact(dd.getGroupId(),
                        dd.getArtifactId(), dd.getClassifier(), dd.getType(), dd.getVersion()))
                .collect(Collectors.toSet());
    }

    private List<Artifact> getBundleDependencies(Dependency dependency) {
        String scope = dependency.getScope() == null ? "compile" : dependency.getScope();

        if (scope.equals("compile")
                || scope.equals("runtime")) {
            return artifactResolver.resolveDependencies(DependencyDescriptor.deriveMavenDependency(dependency))
                    .stream()
                    .filter(artifact ->
                            exclusions.stream()
                                    .noneMatch(exclusion ->
                                            exclusion.getGroupId().equals(artifact.getGroupId())
                                                    && exclusion.getArtifactId().equals(artifact.getArtifactId())))
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    private DependencyDescriptor selectMaxVersionDependency(DependencyDescriptor dd1,
                                                            DependencyDescriptor dd2) {
        DefaultArtifactVersion v1 = new DefaultArtifactVersion(dd1.getVersion());
        DefaultArtifactVersion v2 = new DefaultArtifactVersion(dd2.getVersion());
        return v1.compareTo(v2) > 0 ? dd1 : dd2;
    }

    private void archiveBundle() {
        try {
            if (this.archive.equals(Constants.ARCHIVE_TYPE_TAR)) {
                FileUtils.tarGzFolder(target,
                        target + File.separator + "jstressy.tar.gz", "jstressy.tar.gz", target + "/");
            } else if (this.archive.equals(Constants.ARCHIVE_TYPE_ZIP)) {
                FileUtils.zipFolder(target,
                        target + File.separator + "jstressy.zip", "jstressy.zip", target + "/");
            }
        } catch (IOException e) {
            getLog().error(e);
        }
    }
}
