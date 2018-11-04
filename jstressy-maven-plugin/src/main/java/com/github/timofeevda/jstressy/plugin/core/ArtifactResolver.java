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

package com.github.timofeevda.jstressy.plugin.core;


import com.github.timofeevda.jstressy.plugin.api.Exclusion;
import org.apache.maven.plugin.logging.Log;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;

import java.io.File;
import java.util.List;

/**
 * Artifact resolver for downloading maven artifacts to repository system. After artifact is resolved, it's file can
 * be found in local Maven repository
 *
 * @author timofeevda
 */
public class ArtifactResolver {

    private final Log log;
    /**
     * Maven's repository system
     */
    private RepositorySystem repositorySystem;
    /**
     * Project's remote repositories to use for the resolution of artifact and their dependencies
     */
    private List<RemoteRepository> remoteRepositories;
    /**
     * Maven's repository/network configuration
     */
    private RepositorySystemSession repositorySession;

    /**
     * List of bundles we need to skip
     */
    private List<Exclusion> exclusions;

    public ArtifactResolver(List<RemoteRepository> remoteRepositories,
                            RepositorySystem repositorySystem,
                            RepositorySystemSession repositorySession,
                            List<Exclusion> exclusions,
                            Log log) {
        this.remoteRepositories = remoteRepositories;
        this.repositorySystem = repositorySystem;
        this.repositorySession = repositorySession;
        this.exclusions = exclusions;
        this.log = log;
    }

    /**
     * Resolve artifact using bundle properties
     *
     * @param groupId    artifact's groupId
     * @param artifactId artifact's artifactId
     * @param classifier artifact's classifier
     * @param type       artifact's type
     * @param version    artifact's version
     * @return resolved artifact, null if artifact has not been resolved
     */
    public Artifact resolveArtifact(String groupId, String artifactId,
                                    String classifier, String type, String version) {

        Artifact artifact = new DefaultArtifact(groupId, artifactId, classifier,
                type, version);

        ArtifactRequest artifactRequest = new ArtifactRequest();
        artifactRequest.setRepositories(remoteRepositories);
        artifactRequest.setArtifact(artifact);

        // receive artifact result and resolve it (checking in local repo or downloading it)
        ArtifactResult artifactResult = null;
        try {
            artifactResult = repositorySystem.resolveArtifact(repositorySession, artifactRequest);
        } catch (org.eclipse.aether.resolution.ArtifactResolutionException ex) {
            log.error(ex);
        }

        // create resolved artifact
        Artifact resolvedArtifact = null;
        if (artifactResult != null) {
            // create path to local repository where resolved artifact is located
            File artifactFile = new File(
                    repositorySession.getLocalRepository().getBasedir()
                            + File.separator
                            + repositorySession.getLocalRepositoryManager().getPathForLocalArtifact(artifactRequest.getArtifact()));
            resolvedArtifact = artifactResult.getArtifact().setFile(artifactFile);
        } else {
            log.error("Artifact " + artifact + " has not been resolved.");
        }
        return resolvedArtifact;
    }

    /**
     * Construct dependency graph and resolve dependencies.
     * Return flat list using graph visitor
     *
     * @param dependencyDescriptor {@link DependencyDescriptor}
     * @return flat list with dependencies represented as artifacts
     */
    public List<Artifact> resolveDependencies(DependencyDescriptor dependencyDescriptor) {
        Artifact artifact = dependencyDescriptor.getAetherArtifact();
        Dependency dep = new Dependency(artifact, dependencyDescriptor.getType());
        CollectRequest req = new CollectRequest(dep, remoteRepositories);

        DependenciesFlatteningVisitor rdv = new DependenciesFlatteningVisitor(exclusions);
        try {
            DependencyNode node = repositorySystem.collectDependencies(repositorySession, req).getRoot();
            repositorySystem.resolveDependencies(repositorySession, new DependencyRequest(node, null));
            node.accept(rdv);
        } catch (DependencyResolutionException | DependencyCollectionException ex) {
            log.error(ex);
        }
        return rdv.getArtifacts();
    }
}

