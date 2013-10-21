package net.e175.tools;

import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

/**
 * This filter attempts to fix the results of botched conversions from CP-1252
 * text to UTF-8 for German characters. The problem occurs when a file already
 * encoded as UTF-8 is opened as CP-1252, but then saved again as UTF-8. This
 * results in characteristic two- to three-character gibberish where single
 * characters with diacritical marks, the eszett, or the Euro sign should be.
 * 
 * This idea could be extended to a larger set of characters, but at the moment
 * it's kept to a minimum that's known to work properly here.
 * 
 * Note that this should only be applied to UTF-8 encoded streams!
 * 
 * @author Klaus Brunner
 * 
 */
public final class BrokenGermanUTFConversionFilter implements CharFilter {

    public static final String OLD_ENCODING = "windows-1252";

    private static final String[] SRC_CHARS = { "Ä", "ä", "Ö", "ö", "Ü", "ü", "ß", "€", "é", "á", "à", "è" };

    /** contains regexps */
    private static final String[] BAD_CHARS = new String[SRC_CHARS.length];
    
    static { // static initializer for BAD_CHARS
        try {
            for (int i = 0; i < SRC_CHARS.length; i++) {
                // encode each character to UTF-8
                final byte[] utf8bytes = SRC_CHARS[i].getBytes("UTF-8");

                // now (erroneously) understand each single byte as a single
                // character
                final String badchars = new String(utf8bytes, OLD_ENCODING);

                // now convert this to a regular expression
                BAD_CHARS[i] = "\\Q" + badchars + "\\E";

                Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).fine("fix mapping: " + SRC_CHARS[i] + " " + BAD_CHARS[i]);
            }

        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException("failed to initialize", e);
        }
    }

    private StringBuffer lineBuffer;

    public BrokenGermanUTFConversionFilter() {
        this.reset();
    }

    private String processBuffer() {
        String buffer = this.lineBuffer.toString();

        for (int i = 0; i < BAD_CHARS.length; i++) {
            buffer = buffer.replaceAll(BAD_CHARS[i], SRC_CHARS[i]);
        }

        this.reset();
        return buffer;
    }

    public synchronized String filter(final int character) {
        switch (character) {
        case -1: // end of stream
            return this.processBuffer();
        case '\n': // end of line
        case '\r':
            this.lineBuffer.append((char) character);
            return this.processBuffer();
        default:
            this.lineBuffer.append((char) character);
        }

        return "";
    }

    public synchronized void reset() {
        this.lineBuffer = new StringBuffer();
    }
}
