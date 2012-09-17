package org.jenkinsci.backend.confluence.pageremover;

import hudson.plugins.jira.soap.RemotePage;
import org.jenkinsci.backend.confluence.pageremover.App.PossibleSpamPage;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.xml.rpc.ServiceException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings("Since15")
public class SpamKiller extends JFrame {
    private final MyTableModel model = new MyTableModel();
    private final JTable spams = new JTable(model);
    private final JButton scan = new JButton("Scan");
    private final JButton clean = new JButton("Clean");

    private final App app;
    private List<PossibleSpamPage> possibleSpams = Collections.emptyList();
    private final BitSet spamFlag = new BitSet();

    public static void main(String[] args) throws Exception {
        new SpamKiller();
    }

    public SpamKiller() throws HeadlessException, IOException, ServiceException {
        super("Wiki Spam cleanup");

        app = new App();

        setLayout(new BorderLayout());

        spams.setFillsViewportHeight(true);
        spams.setAutoCreateRowSorter(true);
        spams.setPreferredScrollableViewportSize(new Dimension(300, 100));
        spams.setDefaultRenderer(Number.class,new BarRenderer());

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        add(new JScrollPane(spams));
        add(createButtonPanel(), BorderLayout.PAGE_END);

        pack();
        setVisible(true);

        scan.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scan();
            }
        });
        clean.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clean();
            }
        });
    }

    /**
     * Deletes pages marked as spams.
     */
    private void clean() {
        if (JOptionPane.showConfirmDialog(this,String.format("Are you sure to delete %d pages?",spamFlag.cardinality()),"Confirmation",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)==0) {

            runAsync(new Callable<Void>() {
                public Void call() throws Exception {
                    for (int i=possibleSpams.size()-1; i>=0; i--) {
                        if (spamFlag.get(i)) {
                            possibleSpams.get(i).delete();
                            possibleSpams.remove(i);
                        }
                    }
                    spamFlag.clear();
                    model.fireTableDataChanged();
                    return null;
                }
            });
        }
    }

    /**
     * Looks up spams.
     */
    private void scan() {
        runAsync(new Callable<Void>() {
            public Void call() throws Exception {
                possibleSpams = app.spellCheck();
//                possibleSpams = fakeData();

                spamFlag.clear();
                for (int i=0; i<possibleSpams.size(); i++)
                    if (possibleSpams.get(i).isVeryLikelySpam())
                        spamFlag.set(i,true);

                model.fireTableDataChanged();
                return null;
            }
        });
    }

    private List<PossibleSpamPage> fakeData() {
        List<PossibleSpamPage> l = new ArrayList<PossibleSpamPage>();
        RemotePage pg = new RemotePage();
        pg.setTitle("Title");
        pg.setModifier("somebody");
        l.add(app.new PossibleSpamPage(35, pg));
        return l;
    }

    /**
     * Run a lengthy asynchronous computation with a reasonable UI feedback.
     */
    private void runAsync(final Callable<?> c) {
        setCursor(Cursor.WAIT_CURSOR);
        new SwingWorker<Void,Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    c.call();
                    return null;
                } finally {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            setCursor(Cursor.DEFAULT_CURSOR);
                        }
                    });
                }
            }
        }.execute();

    }

    private JPanel createButtonPanel() {
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(scan);
        buttons.add(clean);
        return buttons;
    }

    class MyTableModel extends AbstractTableModel {
        private String[] columnNames = {"Spam?", "Spam Score", "Title", "Author"};

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return possibleSpams.size();
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            PossibleSpamPage spam = possibleSpams.get(row);
            switch (col) {
            case 0:
                return spamFlag.get(row);
            case 1:
                return spam.score;
            case 2:
                return spam.page.getTitle();
            case 3:
                return spam.page.getModifier();
            default:
                return null;
            }
        }

        public Class getColumnClass(int c) {
            switch (c) {
            case 0:
                return Boolean.class;
            case 1:
                return Number.class;
            default:
                return String.class;
            }
        }

        public boolean isCellEditable(int row, int col) {
            return col==0;
        }

        public void setValueAt(Object value, int row, int col) {
            spamFlag.set(row,(Boolean)value);
            fireTableCellUpdated(row, col);
        }
    }

    public class BarRenderer extends JProgressBar implements TableCellRenderer {
        public BarRenderer() {
            setOpaque(true);
            setMinimum(0);
            setMaximum(100);
        }

        public Component getTableCellRendererComponent(
                                JTable table, Object number,
                                boolean isSelected, boolean hasFocus,
                                int row, int column) {
            setValue(((Number)number).intValue());

            String s = String.format("%05.2f",((Number)number).floatValue());
            setString(s);
            setToolTipText(s);
            setStringPainted(true);
            return this;
        }
    }
}
