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
import org.eclipse.aether.artifact.DefaultArtifact;

/**
 * Utility dependency descriptor to represent Aether and Maven dependencies, implementing convenient equals and hashcode
 *
 * @author timofeevda
 */
public class DependencyDescriptor {

    private String groupId;

    private String artifactId;

    private String classifier;

    private String type;

    private String version;

    private String path;

    private DependencyDescriptor(String groupId, String artifactId, String classifier, String type, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.classifier = classifier;
        this.type = type;
        this.version = version;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getClassifier() {
        return classifier;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getType() {
        return type;
    }

    public String getVersion() {
        return version;
    }

    public String getPath() {
        return path;
    }

    private DependencyDescriptor setPath(String path) {
        this.path = path;
        return this;
    }

    public static DependencyDescriptor deriveAetherArtifact(org.eclipse.aether.artifact.Artifact artifact) {
        return new DependencyDescriptor(artifact.getGroupId(),
                artifact.getArtifactId(), artifact.getClassifier(),
                artifact.getExtension(), artifact.getVersion())
                .setPath(artifact.getFile().getPath());
    }

    public static DependencyDescriptor deriveMavenDependency(org.apache.maven.model.Dependency dependency) {
        return new DependencyDescriptor(dependency.getGroupId(),
                dependency.getArtifactId(), dependency.getClassifier(),
                dependency.getType(), dependency.getVersion());
    }

    public Artifact getAetherArtifact() {
        return new DefaultArtifact(groupId, artifactId, classifier, type, version);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DependencyDescriptor other = (DependencyDescriptor) obj;
        if ((this.groupId == null) ? (other.groupId != null) : !this.groupId.equals(other.groupId)) {
            return false;
        }
        if ((this.artifactId == null) ? (other.artifactId != null) : !this.artifactId.equals(other.artifactId)) {
            return false;
        }
        if ((this.type == null) ? (other.type != null) : !this.type.equals(other.type)) {
            return false;
        }
        if ((this.version == null) ? (other.version != null) : !this.version.equals(other.version)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (this.groupId != null ? this.groupId.hashCode() : 0);
        hash = 47 * hash + (this.artifactId != null ? this.artifactId.hashCode() : 0);
        hash = 47 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 47 * hash + (this.version != null ? this.version.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "DependencyDescriptor{" + "groupId=" + groupId + ", artifactId=" + artifactId + ", classifier=" + classifier + ", type=" + type + ", version=" + version + ", path=" + path + '}';
    }

}

