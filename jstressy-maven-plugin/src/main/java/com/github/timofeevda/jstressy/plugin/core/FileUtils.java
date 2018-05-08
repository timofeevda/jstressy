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

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * File utilities to perform copying, zipping, taring etc.
 *
 * @author timofeevda
 */
public class FileUtils {
    /**
     * Windows folder delimiter to normalize
     */
    private static final String DELIMITER_TO_NORMALIZE = "\\";
    /**
     * Normalization delimiter in unix style
     */
    private static final String DELIMITER_NORMALIZED = "/";

    private static final int MODE_READABLE = 4;

    private static final int MODE_WRITABLE = 2;

    private static final int MODE_EXECUTABLE = 1;

    private static final int MODE_MASK = 0100000;

    private static final int MODE_MASK_USER = 0100;

    private static final int MODE_MASK_GROUP = 010;

    private static final int COPY_BUFFER_SIZE = 1024;

    public static void copyFile(File source, File target) throws IOException {
        CopyFileUtility utility = new CopyFileUtility();
        utility.copyFile(source, target);
    }

    public static boolean deleteFolder(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFolder(file);
                } else {
                    if (!file.delete()) {
                        // suppress warnings about unsuccessful deletion
                        Logger.getLogger(FileUtils.class.getName()).log(Level.WARNING,
                                "Couldn''t delete file: {0}", file.getAbsolutePath());
                    }
                }
            }
        }
        return (path.delete());
    }

    public static void createFolders(File... folders) throws IOException {
        for (File folder : folders) {
            if (!folder.exists()) {
                boolean res = folder.mkdirs();
                if (!res) {
                    throw new IOException("Can't create intermediate distribution folder: " + folder.getAbsolutePath());
                }
            }
        }
    }

    /**
     * Zip folder
     *
     * @param source          path to the source folder
     * @param zipFilePath     path to the zip file
     * @param headReplacement head path to replace in order to get relative
     *                        entries in zip file
     * @throws IOException
     */
    public static void zipFolder(String source,
                                 String zipFilePath, String zipName, String headReplacement) throws IOException {
        ZipFolderUtility zp = new ZipFolderUtility();
        zp.zipFolder(source, zipFilePath, zipName, headReplacement);
    }

    /**
     * Create tar.gz archive from the folder
     *
     * @param source          source folder to archive
     * @param tarGzFilePath   path to the tar.gz file
     * @param headReplacement head path to replace in order to get relative
     *                        entries in zip file
     * @throws IOException
     */
    public static void tarGzFolder(String source,
                                   String tarGzFilePath,
                                   String tarName,
                                   String headReplacement) throws IOException {
        TarGzFolderUtility tp = new TarGzFolderUtility();
        tp.tarGzFolder(source, tarGzFilePath, tarName, headReplacement);
    }

    /**
     * Utility class to keep bytes during recursive folder tree walk
     */
    private static class BytesCounter {

        private long bytes;
    }

    /**
     * Copy folder utility
     */
    private static class CopyFileUtility {

        void copyFile(File in, File out) throws IOException {
            if (out.exists()) {
                boolean result = out.delete();
                if (!result) {
                    System.out.println("Couldn't delete existing file: " + out.getAbsolutePath());
                }
            }
            try (FileChannel inChannel = new FileInputStream(in).getChannel();
                 FileChannel outChannel = new FileOutputStream(out).getChannel()) {
                inChannel.transferTo(0, inChannel.size(), outChannel);
                out.setWritable(in.canWrite());
                out.setReadable(in.canRead());
                out.setExecutable(in.canExecute());
            }
        }
    }

    /**
     * Zip folder utility
     */
    private static class ZipFolderUtility {

        private static final int BUFFER_SIZE = 32768;

        private void zipFolder(String source, String zipFilePath,
                               String zipName,
                               String replacement) throws IOException {

            String normalizedReplacement =
                    getNormalizedPath(replacement);

            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFilePath))) {
                zipDir(source, zos, zipName, normalizedReplacement);
            }
        }

        private void zipDir(String folderToZip,
                            ZipOutputStream zos, String zipName, String replacement) throws IOException {

            File zipDir = new File(folderToZip);

            String[] dirList = zipDir.list();
            byte[] readBuffer = new byte[BUFFER_SIZE];
            int bytesIn;

            for (String dir : dirList) {
                if (dir.equals(zipName)) {
                    continue;
                }
                File f = new File(zipDir, dir);
                if (f.isDirectory()) {
                    String filePath = getNormalizedPath(f.getPath());
                    ZipEntry anEntry = new ZipEntry(
                            subString(getNormalizedPath(f.getPath()) + "/", replacement));
                    zos.putNextEntry(anEntry);
                    zipDir(filePath, zos, zipName, replacement);
                    continue;
                }

                BufferedInputStream fis = new BufferedInputStream(
                        new FileInputStream(f));

                ZipEntry anEntry = new ZipEntry(
                        subString(getNormalizedPath(f.getPath()), replacement));

                zos.putNextEntry(anEntry);

                while ((bytesIn = fis.read(readBuffer)) != -1) {
                    zos.write(readBuffer, 0, bytesIn);
                }
                zos.closeEntry();
                fis.close();
            }
        }
    }

    /**
     * Utility to create tar.gz archive
     */
    protected static class TarGzFolderUtility {

        static final int BUFFER_SIZE = 32768;

        void tarGzFolder(String source, String tarFilePath,
                         String tarName,
                         String replacement) throws IOException {

            String normalizedReplacement =
                    getNormalizedPath(replacement);

            try (TarArchiveOutputStream out = new TarArchiveOutputStream(
                    new GzipCompressorOutputStream(new BufferedOutputStream(new FileOutputStream(tarFilePath))))) {
                out.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
                tarGzFolder(source, out, tarName, normalizedReplacement);
            }
        }

        private void tarGzFolder(String folderToTar,
                                 TarArchiveOutputStream taos,
                                 String tarName,
                                 String replacement) throws IOException {
            File zipDir = new File(folderToTar);
            // get a listing of the directory content
            String[] dirList = zipDir.list();
            byte[] readBuffer = new byte[BUFFER_SIZE];
            int bytesIn;
            // loop through dirList, and zip the files
            for (String dir : dirList) {
                File f = new File(zipDir, dir);
                if (dir.equals(tarName)) {
                    continue;
                }
                if (f.isDirectory()) {
                    String filePath = getNormalizedPath(f.getPath());
                    TarArchiveEntry tae = new TarArchiveEntry(
                            subString(getNormalizedPath(f.getPath()) + "/", replacement));
                    tae.setMode(getMode(f));
                    taos.putArchiveEntry(tae);
                    taos.closeArchiveEntry();
                    tarGzFolder(filePath, taos, tarName, replacement);
                    continue;
                }

                BufferedInputStream fis = new BufferedInputStream(
                        new FileInputStream(f));

                TarArchiveEntry tae = new TarArchiveEntry(
                        subString(getNormalizedPath(f.getPath()), replacement));

                tae.setSize(f.length());

                tae.setMode(getMode(f));

                taos.putArchiveEntry(tae);

                while ((bytesIn = fis.read(readBuffer)) != -1) {
                    taos.write(readBuffer, 0, bytesIn);
                }
                taos.closeArchiveEntry();

                fis.close();
            }
        }
    }

    private static String subString(String source, String headToReplace) {
        if (headToReplace == null) {
            return source;
        }

        if (source.startsWith(headToReplace)) {
            return source.substring(headToReplace.length());
        }

        return source;
    }

    /**
     * Gets normalized path in unix style
     *
     * @param pathToNormalize path to normalize
     * @return path normalized in unix style
     */
    private static String getNormalizedPath(final String pathToNormalize) {
        return pathToNormalize.replace(DELIMITER_TO_NORMALIZE,
                DELIMITER_NORMALIZED);
    }

    private static int getMode(final File in) {
        int mode = 0;
        if (in.canExecute()) {
            mode += MODE_EXECUTABLE;
        }
        if (in.canRead()) {
            mode += MODE_READABLE;
        }
        if (in.canWrite()) {
            mode += MODE_WRITABLE;
        }
        return MODE_MASK | mode * MODE_MASK_USER | mode * MODE_MASK_GROUP | mode;
    }

    public static void copyFile(InputStream in, File out) throws Exception {
        try (BufferedInputStream inB = new BufferedInputStream(in);
             FileOutputStream fos = new FileOutputStream(out)) {
            byte[] buf = new byte[COPY_BUFFER_SIZE];
            int i;
            while ((i = inB.read(buf)) != -1) {
                fos.write(buf, 0, i);
            }
        }
    }
}

