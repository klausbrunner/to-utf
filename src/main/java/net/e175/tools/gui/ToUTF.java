package net.e175.tools.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import net.e175.tools.BrokenGermanUTFConversionFilter;
import net.e175.tools.Convertee;
import net.e175.tools.TreeConverter;

/**
 * A rather primitive GUI for the converter.
 * 
 * Generated using Eclipse Visual Editor.
 * 
 * @author Klaus Brunner
 * 
 */
public final class ToUTF {
	
    private static final String UTF_8 = "UTF-8";

    private static final String APPLICATION_INFO = "To-UTF 1.0.4: A simple converter to UTF-8 encoding. \n K. Brunner, 2006-2009";

    private static final String DEFAULT_DEFAULT_ENCODING = "ISO-8859-1"; // @jve:decl-index=0:

    private static final String BACKUP_FILE_EXTENSION = ".backup";

    private static final boolean SHOW_ALL_ENCODINGS = false;
    
    private static final Logger LOG = Logger.getLogger(ToUTF.class.getName());

    private JFrame jFrame = null; // @jve:decl-index=0:visual-constraint="10,10"

    private JPanel jContentPane = null;

    private JMenuBar jJMenuBar = null;

    private JMenu fileMenu = null;

    private JMenuItem exitMenuItem = null;

    private JMenuItem openMenuItem = null;

    private JButton jRunButton = null;

    private JMenu jMenuSrcEncoding = null;

    private String selectedSourceEncoding = null; // @jve:decl-index=0:

    private TreeConverter converter = null; // @jve:decl-index=0:

    private List convertees = null;

    private JScrollPane jScrollPane = null;

    private JList jList = null;

    private File currentRootDir;

    private JMenu optionsMenu = null;

    private JCheckBoxMenuItem jForceDefaultEncodingMenuItem = null;

    private JCheckBoxMenuItem jCreateBackupsMenuItem = null;

    private JCheckBoxMenuItem jRepairUTFMenuItem = null;

    private JMenuItem jExtensionsMenuItem = null;

    private String[] fileExtensions = { ".java" };

    private JMenu jHelpMenu = null;

    private JMenuItem jAboutMenuItem = null;

    private JCheckBoxMenuItem jStripBomMenuItem = null;

    /**
     * This method initializes openMenuItem
     * 
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getOpenMenuItem() {
        if (openMenuItem == null) {
            openMenuItem = new JMenuItem();
            openMenuItem.setText("Open...");
            openMenuItem.setMnemonic(KeyEvent.VK_O);
            openMenuItem.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    final JFileChooser fc = new JFileChooser();
                    fc.setFileFilter(new FileFilter() {
                        public boolean accept(File f) {
                            return f.isDirectory();
                        }

                        public String getDescription() {
                            return "Directories";
                        }
                    });
                    if (currentRootDir != null && currentRootDir.exists()) {
                        fc.setCurrentDirectory(currentRootDir);
                    }
                    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    fc.setAcceptAllFileFilterUsed(false);
                    if (fc.showOpenDialog(jFrame) == JFileChooser.APPROVE_OPTION) {
                        currentRootDir = fc.getSelectedFile();
                        initializeList(true);
                    }

                }
            });
        }
        return openMenuItem;
    }

    /**
     * This method initializes jRunButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getJRunButton() {
        if (jRunButton == null) {
            jRunButton = new JButton();
            jRunButton.setText("Convert selected files");
            jRunButton.setEnabled(false);
            jRunButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    runConversion();
                }
            });
        }
        return jRunButton;
    }

    /**
     * This method initializes jMenuSrcEncoding
     * 
     * @return javax.swing.JMenu
     */
    private JMenu getJMenuSrcEncoding() {
        if (jMenuSrcEncoding == null) {
            jMenuSrcEncoding = new JMenu();
            jMenuSrcEncoding.setText("Default Source Encoding");

            jMenuSrcEncoding.setToolTipText("The usual encoding of your existing source code (before Unicode conversion)");
            // find out default character set
            final String[] charsets = Convertee.getAvailableCharsets();
            Arrays.sort(charsets);

            String defaultEncName = Convertee.getSystemDefaultEncoding();

            LOG.finer("default encoding detected as " + defaultEncName);

            if (defaultEncName == null) {
                // no default encoding known (unlikely, but possible)
                defaultEncName = DEFAULT_DEFAULT_ENCODING;
            }

            this.setSelectedSourceEncoding(defaultEncName);

            ButtonGroup charsetsGroup = new ButtonGroup();

            for (int i = 0; i < charsets.length; i++) {

                final String cs = charsets[i];

                // filter displayed charsets to a reasonable subset
                // make sure that default charset is displayed in any case
                if (SHOW_ALL_ENCODINGS || cs.equals(defaultEncName) || cs.startsWith("ISO") || cs.startsWith("windows") || cs.startsWith("KOI")) {
                    final JRadioButtonMenuItem mu = new JRadioButtonMenuItem();
                    mu.setText(charsets[i]);
                    if (cs.equals(defaultEncName)) {
                        mu.setSelected(true);
                    }

                    // display tooltips when useful
                    Charset c = Charset.forName(cs);
                    if (c != null && !cs.equals(c.displayName())) {
                            mu.setToolTipText(c.displayName());
                    }

                    // add handler for selection
                    mu.addItemListener(new ItemListener() {
                        public void itemStateChanged(ItemEvent e) {
                            if (e.getStateChange() == ItemEvent.SELECTED) {
                                JMenuItem item = (JMenuItem) e.getItem();
                                setSelectedSourceEncoding(item.getText());
                                initializeList(true); // recreate convertee
                                // list
                            }
                        }
                    });

                    charsetsGroup.add(mu);
                    jMenuSrcEncoding.add(mu);
                }

            }

        }
        return jMenuSrcEncoding;
    }

    /**
     * This method initializes jScrollPane
     * 
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();

            jScrollPane.setViewportView(getJList());
        }
        return jScrollPane;
    }

    /**
     * This method initializes jList
     * 
     * @return javax.swing.JList
     */
    private JList getJList() {
        if (jList == null) {
            jList = new JList();

        }
        return jList;
    }

    /**
     * This method initializes optionsMenu
     * 
     * @return javax.swing.JMenu
     */
    private JMenu getOptionsMenu() {
        if (optionsMenu == null) {
            optionsMenu = new JMenu();
            optionsMenu.setText("Options");
            optionsMenu.add(getJMenuSrcEncoding());
            optionsMenu.add(getJExtensionsMenuItem());            
            optionsMenu.add(getJForceDefaultEncodingMenuItem());
            optionsMenu.add(getJCreateBackupsMenuItem());
            optionsMenu.add(getJStripBomMenuItem());
            optionsMenu.add(getJRepairUTFMenuItem());            
        }
        return optionsMenu;
    }

    /**
     * This method initializes jForceDefaultEncodingMenuItem
     * 
     * @return javax.swing.JCheckBoxMenuItem
     */
    private JCheckBoxMenuItem getJForceDefaultEncodingMenuItem() {
        if (jForceDefaultEncodingMenuItem == null) {
            jForceDefaultEncodingMenuItem = new JCheckBoxMenuItem();
            jForceDefaultEncodingMenuItem.setText("Force Default Encoding");
            jForceDefaultEncodingMenuItem.setToolTipText("Don't try to auto-detect UTF-8, always assume default source encoding");
        }
        return jForceDefaultEncodingMenuItem;
    }

    /**
     * This method initializes jCreateBackupsMenuItem
     * 
     * @return javax.swing.JCheckBoxMenuItem
     */
    private JCheckBoxMenuItem getJCreateBackupsMenuItem() {
        if (jCreateBackupsMenuItem == null) {
            jCreateBackupsMenuItem = new JCheckBoxMenuItem();
            jCreateBackupsMenuItem.setText("Make Backup Copies");
            jCreateBackupsMenuItem.setToolTipText("Backup original files before conversion (" + BACKUP_FILE_EXTENSION + ")");
        }
        return jCreateBackupsMenuItem;
    }

    /**
     * This method initializes jCreateBackupsMenuItem
     * 
     * @return javax.swing.JCheckBoxMenuItem
     */
    private JCheckBoxMenuItem getJRepairUTFMenuItem() {
        if (jRepairUTFMenuItem == null) {
            jRepairUTFMenuItem = new JCheckBoxMenuItem();
            jRepairUTFMenuItem.setText("Repair broken (German) UTF-8");
            jRepairUTFMenuItem
                    .setToolTipText("EXPERIMENTAL: attempts to repair broken UTF-8 conversions of German text. Use backups, check results carefully!");
        }
        return jRepairUTFMenuItem;
    }

    /**
     * This method initializes jExtensionsMenuItem
     * 
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJExtensionsMenuItem() {
        if (jExtensionsMenuItem == null) {
            jExtensionsMenuItem = new JMenuItem();
            jExtensionsMenuItem.setText("File extensions...");
            jExtensionsMenuItem.setToolTipText("Select extensions of files to convert");
            jExtensionsMenuItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    StringBuffer currentExtensions = new StringBuffer();
                    String[] extensions = getFileExtensions();

                    for (int i = 0; i < extensions.length; i++) {
                        currentExtensions.append(extensions[i]);
                        currentExtensions.append(" ");
                    }

                    String s = (String) JOptionPane.showInputDialog(jFrame, "List file extensions (space-separated):", "Set File Extensions",
                            JOptionPane.PLAIN_MESSAGE, null, null, currentExtensions.toString());

                    if (s != null) {
                        s = s.trim();
                        String[] newExt = s.split("\\s+");
                        setFileExtensions(newExt);
                    }

                }
            });
        }
        return jExtensionsMenuItem;
    }

    /**
     * This method initializes jHelpMenu
     * 
     * @return javax.swing.JMenu
     */
    private JMenu getJHelpMenu() {
        if (jHelpMenu == null) {
            jHelpMenu = new JMenu();
            jHelpMenu.setText("Help");
            jHelpMenu.add(getJAboutMenuItem());
        }
        return jHelpMenu;
    }

    /**
     * This method initializes jAboutMenuItem
     * 
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJAboutMenuItem() {
        if (jAboutMenuItem == null) {
            jAboutMenuItem = new JMenuItem();
            jAboutMenuItem.setText("About...");
            jAboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    JOptionPane.showMessageDialog(jFrame, APPLICATION_INFO, "About", JOptionPane.INFORMATION_MESSAGE);

                }
            });
        }
        return jAboutMenuItem;
    }

    /**
     * This method initializes jStripBomMenuItem
     * 
     * @return javax.swing.JCheckBoxMenuItem
     */
    private JCheckBoxMenuItem getJStripBomMenuItem() {
        if (jStripBomMenuItem == null) {
            jStripBomMenuItem = new JCheckBoxMenuItem();
            jStripBomMenuItem.setText("Strip BOMs");
            jStripBomMenuItem.setToolTipText("Strip byte order marks from UTF-8-encoded files");
            jStripBomMenuItem.setSelected(true);
        }
        return jStripBomMenuItem;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {
                    LOG.warning("couldn't change look & feel");
                }
                ToUTF application = new ToUTF();
                application.getJFrame().setVisible(true);
            }
        });
    }

    /**
     * This method initializes jFrame
     * 
     * @return javax.swing.JFrame
     */
    private JFrame getJFrame() {
        if (jFrame == null) {
            jFrame = new JFrame();
            jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            jFrame.setLocation(new Point(100, 100));
            jFrame.setJMenuBar(getJJMenuBar());
            jFrame.setSize(498, 355);
            jFrame.setContentPane(getJContentPane());
            jFrame.setTitle("To-UTF");
        }
        return jFrame;
    }

    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getJRunButton(), BorderLayout.SOUTH);
            jContentPane.add(getJScrollPane(), BorderLayout.CENTER);
        }
        return jContentPane;
    }

    /**
     * This method initializes jJMenuBar
     * 
     * @return javax.swing.JMenuBar
     */
    private JMenuBar getJJMenuBar() {
        if (jJMenuBar == null) {
            jJMenuBar = new JMenuBar();
            jJMenuBar.add(getFileMenu());
            jJMenuBar.add(getOptionsMenu());
            jJMenuBar.add(getJHelpMenu());
        }
        return jJMenuBar;
    }

    /**
     * This method initializes jMenu
     * 
     * @return javax.swing.JMenu
     */
    private JMenu getFileMenu() {
        if (fileMenu == null) {
            fileMenu = new JMenu();
            fileMenu.setText("File");
            fileMenu.setMnemonic(KeyEvent.VK_F);
            fileMenu.add(getOpenMenuItem());
            fileMenu.add(getExitMenuItem());
        }
        return fileMenu;
    }

    /**
     * This method initializes jMenuItem
     * 
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getExitMenuItem() {
        if (exitMenuItem == null) {
            exitMenuItem = new JMenuItem();
            exitMenuItem.setText("Exit");
            exitMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });
        }
        return exitMenuItem;
    }

    /**
     * @return the selectedSourceEncoding
     */
    public String getSelectedSourceEncoding() {
        return selectedSourceEncoding;
    }

    /**
     * @param selectedSourceEncoding
     *            the selectedSourceEncoding to set
     */
    public void setSelectedSourceEncoding(String selectedSourceEncoding) {
        this.selectedSourceEncoding = selectedSourceEncoding;
    }

    /**
     * Display the list of files.
     * 
     * @param select
     *            true if files should be selected
     */
    private void initializeList(boolean select) {
        jFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        converter = new TreeConverter();
        converter.setFileExtensions(this.getFileExtensions());

        convertees = new LinkedList();
        converter.listTree(currentRootDir, convertees);

        DefaultListModel newListModel = new DefaultListModel();
        jList.setModel(newListModel);

        for (Iterator it = convertees.iterator(); it.hasNext();) {
            Convertee c = (Convertee) it.next();

            StringBuffer converteeString = new StringBuffer(c.toString());

            try {
                if (!this.jForceDefaultEncodingMenuItem.isSelected() && c.detectAndSetSourceEncoding()) {
                    converteeString.append(" (");
                    converteeString.append(c.getSourceEncoding());
                } else {
                    converteeString.append(" (assuming ");
                    converteeString.append(this.getSelectedSourceEncoding());
                    c.setSourceEncoding(this.getSelectedSourceEncoding());
                }

                if (c.hasBOM()) {
                    converteeString.append(", BOM detected");
                }

                converteeString.append(")");
            } catch (IOException ex) {
                LOG.log(Level.WARNING, ex.getMessage(), ex);
            }

            c.setStringRepresentation(converteeString.toString());
            newListModel.addElement(c);
        }
        jFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        if (select) {
            jList.getSelectionModel().setSelectionInterval(0, newListModel.getSize() - 1);
        }
        jList.ensureIndexIsVisible(0);

        jRunButton.setEnabled(newListModel.getSize() > 0);
    }

    /**
     * Run the actual conversion process.
     * 
     */
    private void runConversion() {
        jFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ListModel model = jList.getModel();
        int[] selectedIndices = jList.getSelectedIndices();

        for (int i = 0; i < selectedIndices.length; i++) {
            Convertee c = (Convertee) model.getElementAt(selectedIndices[i]);

            if (this.jForceDefaultEncodingMenuItem.isSelected()) {
                // always force selected encoding
                c.setSourceEncoding(getSelectedSourceEncoding());
            } else {
                // override selected encoding if reliably detectable
                if (!c.detectAndSetSourceEncoding()) {
                    c.setSourceEncoding(getSelectedSourceEncoding());
                }
            }

            File tempFile;
            try {
                LOG.fine("converting " + c);
                if (this.jCreateBackupsMenuItem.isSelected()) {
                    File backupFile = new File(c.getFile().getAbsolutePath() + BACKUP_FILE_EXTENSION);
                    LOG.fine("copying to backup file " + backupFile);
                    TreeConverter.copyFile(c.getFile(), backupFile);
                }

                tempFile = File.createTempFile("toutf_", "txt");
                tempFile.deleteOnExit();

                if (this.jRepairUTFMenuItem.isSelected() && c.getSourceEncoding().equals(UTF_8)) {
                    c.setFilter(new BrokenGermanUTFConversionFilter());
                    LOG.fine("applying BrokenGermanUTFConversionFilter to " + c);
                }

                c.setStripBOM(this.jStripBomMenuItem.isSelected());

                c.recode(tempFile);

                c.setFilter(null);

                TreeConverter.copyFile(tempFile, c.getFile());
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "problem converting " + c, ex);
                JOptionPane.showMessageDialog(jFrame, "Error converting " + ex.getMessage(), "Conversion Error", JOptionPane.ERROR_MESSAGE);
            } 
        }

        this.jForceDefaultEncodingMenuItem.setSelected(false);
        initializeList(false);
        jFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     * @return the fileExtensions
     */
    public String[] getFileExtensions() {
        return fileExtensions;
    }

    /**
     * @param fileExtensions
     *            the fileExtensions to set
     */
    public void setFileExtensions(String[] fileExtensions) {
        this.fileExtensions = fileExtensions;
    }

}
