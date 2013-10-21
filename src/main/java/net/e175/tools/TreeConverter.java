package net.e175.tools;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;

/**
 * Runs encoding conversion on an entire directory tree.
 * 
 * @author Klaus Brunner
 */
public final class TreeConverter {

    private String[] fileExtensions;

    private FileFilter fileFilter;

    private final FileFilter directoryFilter = new FileFilter() {
        public boolean accept(File pathname) {
            return pathname.isDirectory();
        }
    };

    private Convertee newConvertee(final File f) {
        return new Convertee(f);
    }

    public TreeConverter() {
        this.setFileExtensions(null);
    }

    /**
     * 
     * @param fileExt The file extensions that should be considered for filtering eligible files.
     */
    public TreeConverter(final String[] fileExt) {
        this.setFileExtensions(fileExt);
    }

    /**
     * Get a list of convertees for the root directory (no conversion takes
     * place!).
     * 
     * @param rootDirectory
     * @param List
     *            of Convertees
     */
    public synchronized void listTree(final File rootDirectory, final List converteeList) {
        if ((rootDirectory == null) || !rootDirectory.isDirectory()) {
            throw new IllegalArgumentException("root directory must not be null and must be a directory");
        }

        if (converteeList == null) {
            throw new IllegalArgumentException("convertee list must not be null");
        }

        final File[] targetFiles = rootDirectory.listFiles(this.fileFilter);
        for (int i = 0; i < targetFiles.length; i++) {
            final Convertee c = this.newConvertee(targetFiles[i]);
            converteeList.add(c);
        }

        // recurse into subdirs
        final File[] subdirs = rootDirectory.listFiles(this.directoryFilter);
        for (int i = 0; i < subdirs.length; i++) {
            this.listTree(subdirs[i], converteeList);
        }
    }

    /**
     * @return the fileExtensions
     */
    public synchronized String[] getFileExtensions() {
        return (this.fileExtensions != null) ? (String[]) this.fileExtensions.clone() : new String[0];
    }

    /**
     * @param fileExtensions
     *            the fileExtensions to set
     */
    public synchronized void setFileExtensions(final String[] fileExt) {
        this.fileExtensions = (fileExt != null) ? (String[]) fileExt.clone() : null;

        this.fileFilter = new FileFilter() {
            public boolean accept(final File pathname) {
                if (!pathname.isFile()) {
                    return false;
                }

                if (TreeConverter.this.fileExtensions != null) {
                    for (int i = 0; i < TreeConverter.this.fileExtensions.length; i++) {
                        if (pathname.toString().endsWith(TreeConverter.this.fileExtensions[i])) {
                            return true;
                        }
                    }
                    return false;
                } else {
                    return true;
                }
            }
        };
    }

    /**
     * Copy source file to target file.
     * 
     * @param source
     * @param target
     * @throws IOException
     */
    public static void copyFile(final File source, final File target) throws IOException {
        final FileChannel srcChannel = new FileInputStream(source).getChannel();
        final FileChannel dstChannel = new FileOutputStream(target).getChannel();

        dstChannel.transferFrom(srcChannel, 0, srcChannel.size());

        srcChannel.close();
        dstChannel.close();
    }

}
