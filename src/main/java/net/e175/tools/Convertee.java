package net.e175.tools;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;

import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;
import org.mozilla.intl.chardet.nsPSMDetector;

/**
 * A class that holds text files to be converted to UTF-8 encoding.
 * 
 * @author Klaus Brunner
 * 
 */
public final class Convertee {

    public static final String DEFAULT_SOURCE_ENCODING = "ISO-8859-1";

    public static final String UTF8_ENCODING = "UTF-8";

    private static final int BOM = 0xFEFF; // Unicode Byte Order Mark

    /**
     * Get an array of available (canonical) character set names.
     * 
     * @return All character sets known to this Java instance.
     */
    public static String[] getAvailableCharsets() {
        final Map map = Charset.availableCharsets();
        final String[] charsets = new String[map.keySet().size()];

        int i = 0;
        for (final Iterator it = map.keySet().iterator(); it.hasNext();) {
            charsets[i++] = (String) it.next();
        }

        return charsets;
    }

    /**
     * Get canonical name of default encoding.
     * 
     * @return canonical name of default encoding or null if not found
     */
    public static String getSystemDefaultEncoding() {
        // this is somewhat clumsy in Java pre 1.5 (which has
        // the convenient Charset.defaultEncoding() method)
        String defaultEncName = System.getProperty("file.encoding");
        if (defaultEncName != null) {
            // we probably got an alias, so get canonical name for this
            // charset
            final Charset c = Charset.forName(defaultEncName);
            if (c != null) {
                defaultEncName = c.name();
            }
        }
        return defaultEncName;
    }

    /**
     * used for charset detection callback only, don't rely on this field
     */
    private String detectedCharset = null;

    private File file;

    private String sourceEncoding = DEFAULT_SOURCE_ENCODING;

    private CharFilter filter;

    private String stringRepresentation = null;

    private boolean stripBOM = true;

    public Convertee(final File f) {
        if (f == null) {
            throw new IllegalArgumentException("file must not be null");
        }
        this.file = f;
    }

    /**
     * Try to guess encoding. This doesn't seem to work very well at the moment,
     * the NS Character Detection code is rather biased towards Asian languages.
     * However, it detects UTF-8 quite reliably.
     * 
     * @return
     * @throws IOException
     */
    protected synchronized String detectCharset() throws IOException {
        this.detectedCharset = null;
        final nsDetector det = new nsDetector(nsPSMDetector.ALL);

        det.Init(new nsICharsetDetectionObserver() {
            public void Notify(final String charset) {
                Convertee.this.detectedCharset = charset;
            }
        });

        FileInputStream in = null;
        try {
            in = new FileInputStream(this.file);

            final byte[] buf = new byte[2048];
            int len;
            boolean done = false;
            boolean isAscii = true;
            while ((len = in.read(buf, 0, buf.length)) != -1) {
                // Check if the stream is only ASCII
                if (isAscii) {
                    isAscii = det.isAscii(buf, len);
                }

                // DoIt if non-ascii and not done yet.
                if (!isAscii && !done) {
                    done = det.DoIt(buf, len, false);
                }
            }
            det.DataEnd();
        } finally {
            if (in != null) {
                in.close();
            }
        }

        if (this.detectedCharset == null) {
            final String prob[] = det.getProbableCharsets();
            if (prob.length > 0) {
                this.detectedCharset = prob[0];
            }
        }

        return this.detectedCharset;
    }

    /**
     * Try to determine the file's encoding from its BOM, if it has one.
     * 
     * @return canonical name of UTF encoding or null if no BOM found
     */
    public synchronized String encodingAccordingToBOM() throws IOException {
        final String[] encodingNames = { "UTF-32BE", "UTF-32LE", "UTF-16BE", "UTF-16LE", "UTF-8" };
        final byte[][] boms = { { 0, 0, (byte) 0xFE, (byte) 0xFF }, // UTF-32BE
                { (byte) 0xFF, (byte) 0xFE, 0, 0 }, // UTF-32LE
                { (byte) 0xFE, (byte) 0xFF }, // UTF-16BE
                { (byte) 0xFF, (byte) 0xFE }, // UTF-16LE
                { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF } // UTF-8
        };

        // set up bom buffer
        final byte[] bomBuffer = new byte[4];

        // read bytes from file
        FileInputStream imp = null;
        final int readLength;
        try {
            imp = new FileInputStream(this.file);
            readLength = imp.read(bomBuffer);
        } finally {
            if (imp != null) {
                imp.close();
            }
        }

        // check matching BOM sequences (rather stupidly, but good enough for a
        // few bytes)
        for (int i = 0; i < boms.length; i++) {
            if (readLength >= boms[i].length) {
                boolean match = true;
                for (int j = 0; j < boms[i].length; j++) {
                    if (bomBuffer[j] != boms[i][j]) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    return encodingNames[i];
                }
            }
        }

        // no matching BOM found
        return null;
    }

    /**
     * @return the file
     */
    public synchronized File getFile() {
        return this.file;
    }

    /**
     * @return the filter
     */
    public synchronized CharFilter getFilter() {
        return this.filter;
    }

    /**
     * @return the sourceEncoding
     */
    public synchronized String getSourceEncoding() {
        return this.sourceEncoding;
    }

    /**
     * Guess whether file is encoded in UTF-8. You may want to check whether
     * it's text before calling this function.
     * 
     * @return true if this file looks like UTF-8.
     * @throws IOException
     */
    public synchronized boolean looksLikeUTF8() throws IOException {
        return UTF8_ENCODING.equals(this.detectCharset());
    }

    /**
     * Check whether this file starts with a byte order mark (BOM).
     * 
     * @return
     */
    public synchronized boolean hasBOM() throws IOException {
        return this.encodingAccordingToBOM() != null;
    }

    /**
     * Recode file using the current source and target encodings to the given
     * target file (may be either a directory or a file, may not be same as
     * source).
     * 
     */
    public synchronized void recode(final File targetFile) throws IOException {
        if ((targetFile == null) || this.file.equals(targetFile)) {
            throw new IllegalArgumentException("target must not be null or same as source");
        }

        String readerEncoding = this.getSourceEncoding();

        File actualTargetFile;
        if (targetFile.isDirectory()) {
            actualTargetFile = new File(targetFile, this.file.getName());
        } else {
            actualTargetFile = targetFile;
        }

        final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.file), readerEncoding));
        final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(actualTargetFile), UTF8_ENCODING));

        if (this.filter != null) {
            this.filter.reset();
        }

        int c;
        boolean firstChar = true;
        do {
            c = reader.read();
            boolean ignoreChar = false;

            // strip BOM if necessary
            if (firstChar) {
                firstChar = false;
                if (this.isStripBOM() && (c == BOM) && readerEncoding.startsWith("UTF-")) {
                    ignoreChar = true;
                }
            }

            // apply filter (if any)
            if (!ignoreChar) {
                if (this.filter != null) {
                    final String fresult = this.filter.filter(c);
                    writer.write(fresult);
                } else if (c != -1) {
                    writer.write(c);
                }
            }

        } while (c != -1);

        reader.close();
        writer.close();

        // we can be reasonably certain that it's UTF-8 now
        this.setSourceEncoding(UTF8_ENCODING);
    }

    /**
     * @param filter
     *            the filter to set
     */
    public synchronized void setFilter(final CharFilter filter) {
        this.filter = filter;
    }

    /**
     * @param sourceEncoding
     *            the sourceEncoding to set
     */
    public synchronized void setSourceEncoding(final String sourceEncoding) {
        this.sourceEncoding = sourceEncoding;
    }

    /**
     * Set the string representation to return by toString()
     * 
     * @see toString()
     * 
     * @param stringRepresentation
     *            the stringRepresentation to set
     */
    public synchronized void setStringRepresentation(final String stringRepresentation) {
        this.stringRepresentation = stringRepresentation;
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public synchronized String toString() {
        if (this.stringRepresentation != null) {
            return this.stringRepresentation;
        }

        if (this.file != null) {
            return this.file.toString();
        }

        return super.toString();
    }

    /**
     * @return whether BOMs should be stripped
     */
    public synchronized boolean isStripBOM() {
        return stripBOM;
    }

    /**
     * @param stripBOM
     *            whether BOMs should be stripped
     */
    public synchronized void setStripBOM(final boolean stripBOM) {
        this.stripBOM = stripBOM;
    }

    /**
     * Try to detect and set the source encoding iff reliably possible.
     * Currently, this is only possible for UTF-8, and other UTF encodings if a
     * BOM is present.
     * 
     * @return true if encoding was detected and set successfully, false
     *         otherwise
     * 
     */
    public synchronized boolean detectAndSetSourceEncoding() {
        try {
            String bom = this.encodingAccordingToBOM();
            if (bom == null || UTF8_ENCODING.equals(bom)) {
                if (this.looksLikeUTF8()) {
                    this.setSourceEncoding(UTF8_ENCODING);
                    return true;
                }
            } else {
                this.setSourceEncoding(bom);
                return true;
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }

}
