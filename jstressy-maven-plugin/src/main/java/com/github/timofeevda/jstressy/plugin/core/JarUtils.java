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

import aQute.lib.osgi.Jar;
import java.io.File;
import java.util.Set;
import java.util.jar.Attributes;

/**
 * Utility function checking if file can be treated as bundle
 *
 * @author timofeevda
 */
public class JarUtils {

    /**
     * Check if specified file is OSGI bundle. Check if specified jar file
     * has one of the following attributes in manifest file:
     * - Bundle-Name
     * - Bundle-Version
     * - Bundle-SymbolicName
     * - Bundle-Vendor
     * @param file specified file
     * @return true if specified file is bundle
     * @throws Exception error while processing jar file
     */
    public static boolean isBundle(File file) throws Exception {
        Jar jar = new Jar(file);
        Attributes.Name bndNameAttribute = new Attributes.Name("Bundle-Name");
        Attributes.Name bndVersionAttribute = new Attributes.Name("Bundle-Version");
        Attributes.Name bndSymbolicNameAttribute = new Attributes.Name("Bundle-SymbolicName");
        Attributes.Name bndVendorAttribute = new Attributes.Name("Bundle-Vendor");

        // jars with no manifest are not bundles :)
        if (jar.getManifest() == null) {
            return false;
        }

        Set<Object> attributes = jar.getManifest().getMainAttributes().keySet();

        return attributes.contains(bndNameAttribute)
                || attributes.contains(bndVendorAttribute)
                || attributes.contains(bndSymbolicNameAttribute)
                || attributes.contains(bndVersionAttribute);
    }

}

