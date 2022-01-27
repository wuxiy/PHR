/*********************************************************************************
 * Copyright (c) 2019 中电健康云科技有限公司
 * 版本      DATE             BY             REMARKS
 * ----  -----------  ---------------  ------------------------------------------
 * 1.0    2020/1/16          wuxi           
 ********************************************************************************/
package cn.intcoder.phr.pacs.dcm4che3.util;

import cn.intcoder.phr.pacs.dcm4che3.exception.StreamIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.stream.ImageInputStream;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Deque;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class FileUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);
    public static final int FILE_BUFFER = 4096;
    private static final int[] ILLEGAL_CHARS = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 34, 42, 47, 58, 60, 62, 63, 92, 124};

    private FileUtil() {
    }

    public static String getValidFileName(String fileName) {
        StringBuilder cleanName = new StringBuilder();
        if (fileName != null) {
            for(int i = 0; i < fileName.length(); ++i) {
                char c = fileName.charAt(i);
                if (Arrays.binarySearch(ILLEGAL_CHARS, c) < 0 && c >= ' ' && (c <= '~' || c >= 160)) {
                    cleanName.append(c);
                }
            }
        }

        return cleanName.toString().trim();
    }

    public static String getValidFileNameWithoutHTML(String fileName) {
        String val = null;
        if (fileName != null) {
            val = fileName.replaceAll("\\<.*?>", "");
        }

        return getValidFileName(val);
    }

    public static void safeClose(AutoCloseable object) {
        if (object != null) {
            try {
                object.close();
            } catch (Exception var2) {
                LOGGER.error("Cannot close AutoCloseable", var2);
            }
        }

    }

    public static File createTempDir(File baseDir) {
        if (baseDir != null) {
            String baseName = String.valueOf(System.currentTimeMillis());

            for(int counter = 0; counter < 1000; ++counter) {
                File tempDir = new File(baseDir, baseName + counter);
                if (tempDir.mkdir()) {
                    return tempDir;
                }
            }
        }

        throw new IllegalStateException("Failed to create directory");
    }

    public static final void deleteDirectoryContents(File dir, int deleteDirLevel, int level) {
        if (dir != null && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                File[] var4 = files;
                int var5 = files.length;

                for(int var6 = 0; var6 < var5; ++var6) {
                    File f = var4[var6];
                    if (f.isDirectory()) {
                        deleteDirectoryContents(f, deleteDirLevel, level + 1);
                    } else {
                        deleteFile(f);
                    }
                }
            }

            if (level >= deleteDirLevel) {
                deleteFile(dir);
            }

        }
    }

    public static void getAllFilesInDirectory(File directory, List<File> files) {
        getAllFilesInDirectory(directory, files, true);
    }

    public static void getAllFilesInDirectory(File directory, List<File> files, boolean recursive) {
        File[] fList = directory.listFiles();
        File[] var4 = fList;
        int var5 = fList.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            File f = var4[var6];
            if (f.isFile()) {
                files.add(f);
            } else if (recursive && f.isDirectory()) {
                getAllFilesInDirectory(f, files, recursive);
            }
        }

    }

    private static boolean deleteFile(File fileOrDirectory) {
        try {
            Files.delete(fileOrDirectory.toPath());
            return true;
        } catch (Exception var2) {
            LOGGER.error("Cannot delete", var2);
            return false;
        }
    }

    public static boolean delete(File fileOrDirectory) {
        if (fileOrDirectory != null && fileOrDirectory.exists()) {
            if (fileOrDirectory.isDirectory()) {
                File[] files = fileOrDirectory.listFiles();
                if (files != null) {
                    File[] var2 = files;
                    int var3 = files.length;

                    for(int var4 = 0; var4 < var3; ++var4) {
                        File child = var2[var4];
                        delete(child);
                    }
                }
            }

            return deleteFile(fileOrDirectory);
        } else {
            return false;
        }
    }

    public static void recursiveDelete(File rootDir) {
        recursiveDelete(rootDir, true);
    }

    public static void recursiveDelete(File rootDir, boolean deleteRoot) {
        if (rootDir != null && rootDir.isDirectory()) {
            File[] childDirs = rootDir.listFiles();
            if (childDirs != null) {
                File[] var3 = childDirs;
                int var4 = childDirs.length;

                for(int var5 = 0; var5 < var4; ++var5) {
                    File f = var3[var5];
                    if (f.isDirectory()) {
                        recursiveDelete(f, false);
                        deleteFile(f);
                    } else {
                        deleteFile(f);
                    }
                }
            }

            if (deleteRoot) {
                deleteFile(rootDir);
            }

        }
    }

    public static void safeClose(XMLStreamWriter writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (XMLStreamException var2) {
                LOGGER.error("Cannot close XMLStreamWriter", var2);
            }
        }

    }

    public static void safeClose(XMLStreamReader xmler) {
        if (xmler != null) {
            try {
                xmler.close();
            } catch (XMLStreamException var2) {
                LOGGER.error("Cannot close XMLStreamException", var2);
            }
        }

    }

    public static void prepareToWriteFile(File file) throws IOException {
        if (!file.exists()) {
            File outputDir = file.getParentFile();
            if (outputDir != null && !outputDir.exists() && !outputDir.mkdirs()) {
                throw new IOException("Cannot write parent directory of " + file.getPath());
            }
        }

    }

    public static String nameWithoutExtension(String fn) {
        if (fn == null) {
            return null;
        } else {
            int i = fn.lastIndexOf(46);
            return i > 0 ? fn.substring(0, i) : fn;
        }
    }

    public static String getExtension(String fn) {
        if (fn == null) {
            return "";
        } else {
            int i = fn.lastIndexOf(46);
            return i > 0 ? fn.substring(i) : "";
        }
    }

    public static void writeStreamWithIOException(InputStream inputStream, File outFile) throws StreamIOException {
        try {
            FileOutputStream outputStream = new FileOutputStream(outFile);

            try {
                byte[] buf = new byte[4096];

                while(true) {
                    int offset;
                    if ((offset = inputStream.read(buf)) <= 0) {
                        outputStream.flush();
                        break;
                    }

                    outputStream.write(buf, 0, offset);
                }
            } catch (Throwable var11) {
                try {
                    outputStream.close();
                } catch (Throwable var10) {
                    var11.addSuppressed(var10);
                }

                throw var11;
            }

            outputStream.close();
        } catch (IOException var12) {
            delete(outFile);
            throw new StreamIOException(var12);
        } finally {
            safeClose((AutoCloseable)inputStream);
        }

    }

    public static int writeStream(InputStream inputStream, File outFile) throws StreamIOException {
        return writeStream(inputStream, outFile, true);
    }

    public static int writeStream(InputStream inputStream, File outFile, boolean closeInputStream) throws StreamIOException {
        int var4;
        try {
            FileOutputStream outputStream = new FileOutputStream(outFile);

            byte var6;
            try {
                byte[] buf = new byte[4096];

                while(true) {
                    int offset;
                    if ((offset = inputStream.read(buf)) <= 0) {
                        outputStream.flush();
                        var6 = -1;
                        break;
                    }

                    outputStream.write(buf, 0, offset);
                }
            } catch (Throwable var15) {
                try {
                    outputStream.close();
                } catch (Throwable var14) {
                    var15.addSuppressed(var14);
                }

                throw var15;
            }

            outputStream.close();
            return var6;
        } catch (SocketTimeoutException var16) {
            delete(outFile);
            throw new StreamIOException(var16);
        } catch (InterruptedIOException var17) {
            delete(outFile);
            LOGGER.error("Interruption when writing file: {}", var17.getMessage());
            var4 = var17.bytesTransferred;
        } catch (IOException var18) {
            delete(outFile);
            throw new StreamIOException(var18);
        } finally {
            if (closeInputStream) {
                safeClose((AutoCloseable)inputStream);
            }

        }

        return var4;
    }

    public static int writeFile(ImageInputStream inputStream, File outFile) throws StreamIOException {
        int var3;
        try {
            FileOutputStream outputStream = new FileOutputStream(outFile);

            byte var5;
            try {
                byte[] buf = new byte[4096];

                int offset;
                while((offset = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, offset);
                }

                outputStream.flush();
                var5 = -1;
            } catch (Throwable var14) {
                try {
                    outputStream.close();
                } catch (Throwable var13) {
                    var14.addSuppressed(var13);
                }

                throw var14;
            }

            outputStream.close();
            return var5;
        } catch (SocketTimeoutException var15) {
            delete(outFile);
            throw new StreamIOException(var15);
        } catch (InterruptedIOException var16) {
            delete(outFile);
            LOGGER.error("Interruption when writing image {}", var16.getMessage());
            var3 = var16.bytesTransferred;
        } catch (IOException var17) {
            delete(outFile);
            throw new StreamIOException(var17);
        } finally {
            safeClose((AutoCloseable)inputStream);
        }

        return var3;
    }

    public static String humanReadableByte(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < (long)unit) {
            return bytes + " B";
        } else {
            int exp = (int)(Math.log((double)bytes) / Math.log((double)unit));
            String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
            return String.format("%.1f %sB", (double)bytes / Math.pow((double)unit, (double)exp), pre);
        }
    }

    public static boolean nioWriteFile(FileInputStream inputStream, FileOutputStream out) {
        if (inputStream != null && out != null) {
            boolean var3;
            try {
                FileChannel fci = inputStream.getChannel();

                boolean var4;
                try {
                    FileChannel fco = out.getChannel();

                    try {
                        fco.transferFrom(fci, 0L, fci.size());
                        var4 = true;
                    } catch (Throwable var15) {
                        if (fco != null) {
                            try {
                                fco.close();
                            } catch (Throwable var14) {
                                var15.addSuppressed(var14);
                            }
                        }

                        throw var15;
                    }

                    if (fco != null) {
                        fco.close();
                    }
                } catch (Throwable var16) {
                    if (fci != null) {
                        try {
                            fci.close();
                        } catch (Throwable var13) {
                            var16.addSuppressed(var13);
                        }
                    }

                    throw var16;
                }

                if (fci != null) {
                    fci.close();
                }

                return var4;
            } catch (Exception var17) {
                LOGGER.error("Write file", var17);
                var3 = false;
            } finally {
                safeClose((AutoCloseable)inputStream);
                safeClose((AutoCloseable)out);
            }

            return var3;
        } else {
            return false;
        }
    }

    public static boolean nioWriteFile(InputStream in, OutputStream out, int bufferSize) {
        if (in != null && out != null) {
            boolean var4;
            try {
                ReadableByteChannel readChannel = Channels.newChannel(in);

                boolean var6;
                try {
                    WritableByteChannel writeChannel = Channels.newChannel(out);

                    try {
                        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);

                        while(true) {
                            if (readChannel.read(buffer) == -1) {
                                var6 = true;
                                break;
                            }

                            LangUtil.safeBufferType(buffer).flip();
                            writeChannel.write(buffer);
                            LangUtil.safeBufferType(buffer).clear();
                        }
                    } catch (Throwable var16) {
                        if (writeChannel != null) {
                            try {
                                writeChannel.close();
                            } catch (Throwable var15) {
                                var16.addSuppressed(var15);
                            }
                        }

                        throw var16;
                    }

                    if (writeChannel != null) {
                        writeChannel.close();
                    }
                } catch (Throwable var17) {
                    if (readChannel != null) {
                        try {
                            readChannel.close();
                        } catch (Throwable var14) {
                            var17.addSuppressed(var14);
                        }
                    }

                    throw var17;
                }

                if (readChannel != null) {
                    readChannel.close();
                }

                return var6;
            } catch (IOException var18) {
                LOGGER.error("Write file", var18);
                var4 = false;
            } finally {
                safeClose((AutoCloseable)in);
                safeClose((AutoCloseable)out);
            }

            return var4;
        } else {
            return false;
        }
    }

    public static boolean nioCopyFile(File source, File destination) {
        if (source != null && destination != null) {
            try {
                Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return true;
            } catch (Exception var3) {
                LOGGER.error("Copy file", var3);
                return false;
            }
        } else {
            return false;
        }
    }

    public static Properties readProperties(File propsFile, Properties props) {
        Properties p = props == null ? new Properties() : props;
        if (propsFile != null && propsFile.canRead()) {
            try {
                FileInputStream fileStream = new FileInputStream(propsFile);

                try {
                    p.load(fileStream);
                } catch (Throwable var7) {
                    try {
                        fileStream.close();
                    } catch (Throwable var6) {
                        var7.addSuppressed(var6);
                    }

                    throw var7;
                }

                fileStream.close();
            } catch (IOException var8) {
                LOGGER.error("Error when reading properties", var8);
            }
        }

        return p;
    }

    public static void storeProperties(File propsFile, Properties props, String comments) {
        if (props != null && propsFile != null) {
            try {
                FileOutputStream fout = new FileOutputStream(propsFile);

                try {
                    props.store(fout, comments);
                } catch (Throwable var7) {
                    try {
                        fout.close();
                    } catch (Throwable var6) {
                        var7.addSuppressed(var6);
                    }

                    throw var7;
                }

                fout.close();
            } catch (IOException var8) {
                LOGGER.error("Error when writing properties", var8);
            }
        }

    }

    public static void zip(File directory, File zipfile) throws IOException {
        if (zipfile != null && directory != null) {
            URI base = directory.toURI();
            Deque<File> queue = new LinkedList();
            queue.push(directory);
            FileOutputStream out = new FileOutputStream(zipfile);

            try {
                ZipOutputStream zout = new ZipOutputStream(out);

                try {
                    while(!queue.isEmpty()) {
                        File dir = (File)queue.pop();
                        File[] var7 = dir.listFiles();
                        int var8 = var7.length;

                        for(int var9 = 0; var9 < var8; ++var9) {
                            File entry = var7[var9];
                            String name = base.relativize(entry.toURI()).getPath();
                            if (entry.isDirectory()) {
                                queue.push(entry);
                                if (entry.list().length == 0) {
                                    name = name.endsWith("/") ? name : name + "/";
                                    zout.putNextEntry(new ZipEntry(name));
                                }
                            } else {
                                zout.putNextEntry(new ZipEntry(name));
                                copyZip((File)entry, (OutputStream)zout);
                                zout.closeEntry();
                            }
                        }
                    }
                } catch (Throwable var14) {
                    try {
                        zout.close();
                    } catch (Throwable var13) {
                        var14.addSuppressed(var13);
                    }

                    throw var14;
                }

                zout.close();
            } catch (Throwable var15) {
                try {
                    out.close();
                } catch (Throwable var12) {
                    var15.addSuppressed(var12);
                }

                throw var15;
            }

            out.close();
        }
    }

    public static void unzip(InputStream inputStream, File directory) throws IOException {
        if (inputStream != null && directory != null) {
            try {
                BufferedInputStream bufInStream = new BufferedInputStream(inputStream);

                try {
                    ZipInputStream zis = new ZipInputStream(bufInStream);

                    ZipEntry entry;
                    try {
                        while((entry = zis.getNextEntry()) != null) {
                            File file = new File(directory, entry.getName());
                            if (entry.isDirectory()) {
                                file.mkdirs();
                            } else {
                                file.getParentFile().mkdirs();
                                copyZip((InputStream)zis, (File)file);
                            }
                        }
                    } catch (Throwable var14) {
                        try {
                            zis.close();
                        } catch (Throwable var13) {
                            var14.addSuppressed(var13);
                        }

                        throw var14;
                    }

                    zis.close();
                } catch (Throwable var15) {
                    try {
                        bufInStream.close();
                    } catch (Throwable var12) {
                        var15.addSuppressed(var12);
                    }

                    throw var15;
                }

                bufInStream.close();
            } finally {
                safeClose((AutoCloseable)inputStream);
            }

        }
    }

    public static void unzip(File zipfile, File directory) throws IOException {
        if (zipfile != null && directory != null) {
            ZipFile zfile = new ZipFile(zipfile);

            try {
                Enumeration entries = zfile.entries();

                while(entries.hasMoreElements()) {
                    ZipEntry entry = (ZipEntry)entries.nextElement();
                    File file = new File(directory, entry.getName());
                    if (entry.isDirectory()) {
                        file.mkdirs();
                    } else {
                        file.getParentFile().mkdirs();
                        InputStream in = zfile.getInputStream(entry);

                        try {
                            copyZip(in, file);
                        } catch (Throwable var11) {
                            if (in != null) {
                                try {
                                    in.close();
                                } catch (Throwable var10) {
                                    var11.addSuppressed(var10);
                                }
                            }

                            throw var11;
                        }

                        if (in != null) {
                            in.close();
                        }
                    }
                }
            } catch (Throwable var12) {
                try {
                    zfile.close();
                } catch (Throwable var9) {
                    var12.addSuppressed(var9);
                }

                throw var12;
            }

            zfile.close();
        }
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        if (in != null && out != null) {
            byte[] buf = new byte[4096];

            int offset;
            while((offset = in.read(buf)) > 0) {
                out.write(buf, 0, offset);
            }

            out.flush();
        }
    }

    private static void copyZip(File file, OutputStream out) throws IOException {
        FileInputStream in = new FileInputStream(file);

        try {
            copy(in, out);
        } catch (Throwable var6) {
            try {
                in.close();
            } catch (Throwable var5) {
                var6.addSuppressed(var5);
            }

            throw var6;
        }

        in.close();
    }

    private static void copyZip(InputStream in, File file) throws IOException {
        FileOutputStream out = new FileOutputStream(file);

        try {
            copy(in, out);
        } catch (Throwable var6) {
            try {
                out.close();
            } catch (Throwable var5) {
                var6.addSuppressed(var5);
            }

            throw var6;
        }

        out.close();
    }
}
