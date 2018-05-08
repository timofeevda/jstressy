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

import org.eclipse.aether.artifact.Artifact;

/**
 * Useful utilities for constructing artifact file names
 *
 * @author timofeevda
 */
public class ArtifactFileNameUtils {

    /**
     * Get artifact filename using Aether artifact instance
     *
     * @param artifact
     * @return artifact filename constructed using Aether artifact instance
     */
    public static String getArtifactFileName(Artifact artifact) {
        return getFileName(artifact.getGroupId(),
                artifact.getArtifactId(),
                artifact.getVersion());
    }

    /**
     * Get artifact filename using dependency descriptor instance
     *
     * @param dependencyDescriptor
     * @return artifact filename constructed using dependency descriptor instance
     */
    public static String getArtifactFileName(DependencyDescriptor dependencyDescriptor) {
        return getFileName(dependencyDescriptor.getGroupId(),
                dependencyDescriptor.getArtifactId(),
                dependencyDescriptor.getVersion());
    }

    /**
     * Get artifact filename based on groupId, artifactId and version
     *
     * @param groupId
     * @param artifactId
     * @param version
     * @return artifact filename based on groupId, artifactId and version
     */
    private static String getFileName(String groupId,
                                      String artifactId, String version) {
        return groupId + '.' + artifactId + '_' + version + ".jar";
    }
}

