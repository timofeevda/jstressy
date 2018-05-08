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
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.graph.DependencyVisitor;

import java.util.LinkedList;
import java.util.List;

/**
 * Visitor creating flat list of dependencies based on dependency graph being visited
 *
 * @author timofeevda
 */
public class DependenciesFlatteningVisitor implements DependencyVisitor {

    private List<Artifact> artifactSet = new LinkedList<>();

    private final List<Exclusion> exclusions;

    DependenciesFlatteningVisitor(List<Exclusion> exclusions) {
        this.exclusions = exclusions;
    }

    @Override
    public boolean visitEnter(DependencyNode dn) {
        Dependency dependency = dn.getDependency();
        Artifact artifact = dependency.getArtifact();

        boolean isExcluded = exclusions.stream()
                .anyMatch(exclusion -> artifact.getGroupId().equals(exclusion.getGroupId())
                        && artifact.getArtifactId().equals(exclusion.getArtifactId())
                        && artifact.getVersion().equals(exclusion.getVersion()));

        if (dependency.isOptional() || isExcluded) {
            return true;
        }

        if (dependency.getScope().equals("compile")
                || dependency.getScope().equals("runtime")
                || dependency.getScope().equals("jar")) {
            if (!artifact.getExtension().equals("pom")) {
                artifactSet.add(dn.getDependency().getArtifact());
            }
        }

        return true;
    }

    @Override
    public boolean visitLeave(DependencyNode dn) {
        return true;
    }

    List<Artifact> getArtifacts() {
        return artifactSet;
    }

}

