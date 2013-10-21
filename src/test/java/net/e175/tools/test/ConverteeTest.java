package net.e175.tools.test;

import java.io.File;

import junit.framework.TestCase;
import net.e175.tools.Convertee;

public class ConverteeTest extends TestCase {

    public ConverteeTest() {
        super();
    }

    public void testLooksLikeUTF8() throws Exception {
        final String[] filenames = { "de_iso8859-1.txt", "de_utf-8.txt", "ro_ibm852.txt", "ro_utf-8.txt", "ro_windows1250.txt", "sr_iso8859-5.txt",
                "sr_utf-8.txt" };

        final boolean[] utf8 = { false, true, false, true, false, false, true };

        for (int i = 0; i < filenames.length; i++) {
            final File f = Support.getFile(filenames[i]);
            final Convertee c = new Convertee(f);
            assertEquals(utf8[i], c.looksLikeUTF8());
        }
    }
    
    public void testBomDetection() throws Exception {
        
        File f;
        Convertee c;
        
        f = Support.getFile("utf_8_bom.txt");
        c = new Convertee(f);
        assertEquals("UTF-8", c.encodingAccordingToBOM());
        assertTrue(c.detectAndSetSourceEncoding());
        assertEquals("UTF-8", c.getSourceEncoding());


        f = Support.getFile("de_utf-8-ok_bom.txt");
        c = new Convertee(f);
        assertEquals("UTF-8", c.encodingAccordingToBOM());
        assertTrue(c.detectAndSetSourceEncoding());
        assertEquals("UTF-8", c.getSourceEncoding());
        
        
        f = Support.getFile("utf_16be_bom.txt");
        c = new Convertee(f);
        assertEquals("UTF-16BE", c.encodingAccordingToBOM());
        assertTrue(c.detectAndSetSourceEncoding());
        assertEquals("UTF-16BE", c.getSourceEncoding());
        
        f = Support.getFile("de_utf-8-ok.txt");
        c = new Convertee(f);
        assertNull(c.encodingAccordingToBOM());
        assertTrue(c.detectAndSetSourceEncoding());
        assertEquals("UTF-8", c.getSourceEncoding());
        
    }
    
    public void testBomStripping() throws Exception {
        
        File f = Support.getFile("de_utf-8-ok_bom.txt");
        Convertee c = new Convertee(f);
        c.setSourceEncoding("UTF-8");
        c.setStripBOM(true);

        File target = File.createTempFile("testBomStripping", "txt");
        target.deleteOnExit();
        c.recode(target);
        assertTrue(Support.fileContentsEqual(target, Support.getFile("de_utf-8-ok.txt")));
        
        // --------------------------------------------------
        
        f = Support.getFile("de_utf-16be-ok_bom.txt");
        c = new Convertee(f);
        assertTrue(c.detectAndSetSourceEncoding());
        c.setStripBOM(true);

        target = File.createTempFile("testBomStripping", "txt");
        target.deleteOnExit();
        c.recode(target);
        assertTrue(Support.fileContentsEqual(target, Support.getFile("de_utf-8-ok.txt")));

        
    }

    public void testRecoding() throws Exception {

        File f = Support.getFile("de_iso8859-1.txt");
        Convertee c = new Convertee(f);
        c.setSourceEncoding("ISO-8859-1");

        File target = File.createTempFile("testRecoding", "txt");
        target.deleteOnExit();
        c.recode(target);
        assertTrue("converted file should be identical to reference", Support.fileContentsEqual(target, Support.getFile("de_utf-8.txt")));

        // --------------------------------------------------

        f = Support.getFile("de_utf-8.txt");
        c = new Convertee(f);
        assertTrue(c.detectAndSetSourceEncoding());

        target = File.createTempFile("testRecoding", "txt");
        target.deleteOnExit();
        c.recode(target);
        assertTrue("utf-8 file should remain unchanged", Support.fileContentsEqual(target, Support.getFile("de_utf-8.txt")));

        // --------------------------------------------------

        f = Support.getFile("ro_windows1250.txt");
        c = new Convertee(f);
        c.setSourceEncoding("windows-1250");

        target = File.createTempFile("testRecoding", "txt");
        target.deleteOnExit();
        c.recode(target);
        assertTrue("converted file should be identical to reference", Support.fileContentsEqual(target, Support.getFile("ro_utf-8.txt")));

        // --------------------------------------------------

        f = Support.getFile("sr_iso8859-5.txt");
        c = new Convertee(f);
        c.setSourceEncoding("ISO-8859-5");

        target = File.createTempFile("testRecoding", "txt");
        target.deleteOnExit();
        c.recode(target);
        assertTrue("converted file should be identical to reference", Support.fileContentsEqual(target, Support.getFile("sr_utf-8.txt")));

    }

    public void testAvailableCharsets() {
        final String[] charsets = Convertee.getAvailableCharsets();

        assertNotNull(charsets);
        assertTrue(charsets.length > 0);

        final String defaultCharset = Convertee.getSystemDefaultEncoding();
        assertNotNull(defaultCharset);

        boolean found = false;
        for (int i = 0; i < charsets.length; i++) {
            if (defaultCharset.equals(charsets[i])) {
                found = true;
                break;
            }
        }

        assertTrue("default character set not found in list of charsets?", found);

    }

}
