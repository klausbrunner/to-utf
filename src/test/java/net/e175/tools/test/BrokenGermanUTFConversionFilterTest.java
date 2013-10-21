package net.e175.tools.test;

import java.io.File;

import junit.framework.TestCase;
import net.e175.tools.BrokenGermanUTFConversionFilter;
import net.e175.tools.Convertee;

public class BrokenGermanUTFConversionFilterTest extends TestCase {

    public BrokenGermanUTFConversionFilterTest(String name) {
        super(name);
    }

    public void testInitialization() {
        BrokenGermanUTFConversionFilter filter = new BrokenGermanUTFConversionFilter();
        filter.reset();
    }

    public void testConversion() throws Exception {
        File f = Support.getFile("de_utf-8-broken.txt");
        Convertee c = new Convertee(f);
        c.setSourceEncoding("UTF-8");
        c.setFilter(new BrokenGermanUTFConversionFilter());

        File target = File.createTempFile("testRecodingRepair", "txt");
        target.deleteOnExit();
        c.recode(target);
        assertTrue("converted file should be identical to reference", Support.fileContentsEqual(target, Support.getFile("de_utf-8-ok.txt")));

    }
}
