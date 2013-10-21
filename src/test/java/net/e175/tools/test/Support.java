package net.e175.tools.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;

/**
 * Some support methods for test cases.
 * 
 * @author Klaus Brunner
 */
class Support {

    static File getFile(String filename) throws Exception {
        final URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
        final File f = new File(new URI(url.toString())); // J2SE 5+:
                                                            // url.toURI()
        return f;
    }

    static boolean fileContentsEqual(File f1, File f2) throws IOException {
        if ((f1 == null) || (f2 == null)) {
            return false;
        }

        if (f1.length() != f2.length()) {
            return false;
        }

        final BufferedReader reader1 = new BufferedReader(new InputStreamReader(new FileInputStream(f1)));
        final BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(f2)));

        try {
            int c1, c2;
            do {
                c1 = reader1.read();
                c2 = reader2.read();
                if (c1 != c2) {
                    return false;
                }
            } while (c1 != -1);
        } finally {
            reader1.close();
            reader2.close();
        }

        return true;
    }

}
