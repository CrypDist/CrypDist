package GUI;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Vector;

import Blockchain.Transaction;
import Util.Config;
import org.apache.log4j.Logger;
/**
 * Created by gizem on 06.04.2017.
 */
public class MainScreen extends JPanel {
    JTable blockTable;
    JPanel blockContent;
    GlossyButton upload;
    GlossyButton query;
    GlossyButton refresh;
    ScreenManager controller;
    NonEditableModel tableModel;
    ArrayList<Transaction> currTransactions;
    JTable content;
    DefaultTableModel resultsModel;
    static Logger log = Logger.getLogger("GUI");

    // blocklist numberOfBlocksX2 blocklist[i][0] id of ith block, blocklist[i][1] timestamp of ith block
    public  MainScreen(ScreenManager controller)
    {
        this.controller = controller;
        String[][] blocklist = new String[0][];
        try {
            blocklist = controller.getBlockList();
        } catch (NoSuchAlgorithmException e) {
            log.debug(e);
        }
        setSize(new Dimension(1000,600));

        JPanel buttonsPanel = new JPanel(new GridLayout(0,1));
        upload = new GlossyButton("Upload");
        query = new GlossyButton("Query");
        refresh = new GlossyButton("Refresh");

        currTransactions = new ArrayList<>();
        currTransactions.add(null);
        if(controller.isAuthenticated())
            upload.setEnabled(true);
        else
            upload.setEnabled(false);

        ButtonListener listener = new ButtonListener();
        upload.addActionListener(listener);
        query.addActionListener(listener);
        refresh.addActionListener(listener);

        FlowLayout buttonsLayout = new FlowLayout(FlowLayout.CENTER);
        buttonsLayout.setHgap(60);
        buttonsLayout.setVgap(10);
        buttonsPanel.setLayout(buttonsLayout);
        buttonsPanel.setBackground(Color.white);
        buttonsPanel.add(upload);
        buttonsPanel.add(query);
        buttonsPanel.add(refresh);
        buttonsPanel.setComponentOrientation(
                ComponentOrientation.LEFT_TO_RIGHT);

        JPanel blockPanel = new JPanel();
        GridLayout blockLayout = new GridLayout(0,2);
        blockPanel.setLayout(blockLayout);
        blockPanel.setBackground(Color.white);
        JScrollPane scrollPane = null;
        blockContent = new JPanel(new GridLayout(0,1));
        blockContent.setBackground(Color.white);

        resultsModel = new DefaultTableModel() {
            public boolean isCellEditable(int row, int col)
            {
                return false;
            }
        };
        resultsModel.addColumn("");
        content = new JTable(resultsModel);
        JScrollPane scrollPanex = new JScrollPane(content);
        blockContent.add(scrollPanex);

        DefaultTableCellRenderer moreRenderer = new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table,
                                                           Object value, boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                        row, column);
                setBackground(Color.WHITE);
                if(row % 5 == 3)
                    setForeground(Color.BLUE);
                else
                    setForeground(Color.BLACK);

                return this;
            }

        };
        TableColumnModel jTableColumnModel = content.getColumnModel();
        jTableColumnModel.getColumn(0).setCellRenderer(moreRenderer);
        content.setShowGrid(false);
        content.addMouseListener(new TableListener());

        if(blocklist != null) {
            UIDefaults defaults = UIManager.getLookAndFeelDefaults();
            if (defaults.get("Table.alternateRowColor") == null)
                defaults.put("Table.alternateRowColor", new Color(240, 240, 240));
            Object columnNames[] = {"blockId", "timestamp"};
            tableModel = new NonEditableModel(blocklist, columnNames);
            blockTable = new JTable(tableModel);
            blockTable.setGridColor(Color.white);
            blockTable.setRowHeight(getHeight()/15);
            blockTable.setSelectionModel(new ForcedListSelectionModel());
            scrollPane = new JScrollPane(blockTable);
            scrollPane.setBackground(Color.white);
            add(scrollPane);
            blockTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
                public void valueChanged(ListSelectionEvent event) {
                    if (blockTable.getSelectedRow() > -1) {
                        // print first column value from selected row
                        // log.trace(blockTable.getValueAt(blockTable.getSelectedRow(), 0).toString());
                        String blockId = blockTable.getValueAt(blockTable.getSelectedRow(), 0).toString();
                        displayBlockContent(blockId);
                    }
                }
            });
        }
        blockPanel.add(scrollPane);
        blockPanel.add(blockContent);

        this.setLayout(new BorderLayout());
        this.add(buttonsPanel,BorderLayout.NORTH);
        this.add(blockPanel);
        repaint();
        setVisible(true);
    }

    private void displayBlockContent(String blockId) {
        ArrayList<Transaction> contentx = controller.getBlockContent(blockId);
        currTransactions.clear();
        resultsModel.setRowCount(0);
        for(int i = 0; i < contentx.size(); i++) {
            Object[] header = {"Transaction" + (i+1) + "\n"};
            resultsModel.addRow(header);
            Object[] summary = {"    Summary: " + contentx.get(i).getDataSummary() + "\n"};
            resultsModel.addRow(summary);
            Object[] username = {"   Username: " + contentx.get(i).getSignature() + "\n"};
            resultsModel.addRow(username);
            Object[] filename = {"    Filename: " + contentx.get(i).getFileName() + "\n"};
            resultsModel.addRow(filename);
            Object[] space = {" "};
            resultsModel.addRow(space);
            currTransactions.add(contentx.get(i));
            resultsModel.fireTableDataChanged();
        }
        blockContent.repaint();
    }

    class TableListener extends MouseAdapter
    {
        public void mouseClicked(MouseEvent event) {
            if (content.rowAtPoint(event.getPoint()) % 5 == 3) {
                String filename = (String)content.getValueAt(content.rowAtPoint(event.getPoint()),0);
                filename = filename.substring(14);
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnVal = fileChooser.showOpenDialog(MainScreen.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    String path = fileChooser.getSelectedFile().getAbsolutePath();
                    path = path + "/" + filename ;
                    filename += currTransactions.get(content.rowAtPoint(event.getPoint()) % 5).getVersion();
                    controller.showDownload(filename, path);
                }

            }
        }
    }
    class ButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {

            if(e.getSource() == upload) {
                controller.setCurrentView(new DataUploadScreen(controller));
                controller.setSize((new Dimension(600,300)));
            }
            else if(e.getSource() == query) {
                controller.setCurrentView(new QueryScreen(controller));
                controller.setSize((new Dimension(1000,600)));
            }
            else if (e.getSource() == refresh)
            {
                log.trace("REFRESH CALL");
                String[][] blocklist = new String[0][];
                try {
                    blocklist = controller.getBlockList();
                } catch (NoSuchAlgorithmException e1) {
                    e1.printStackTrace();
                }
                tableModel.setRowCount(0);

                for (int i = 0; i < blocklist.length; i++)
                {
                    Vector<String> rowVector = new Vector<>();
                    rowVector.add(blocklist[i][0]);
                    rowVector.add(blocklist[i][1]);
                    tableModel.addRow(rowVector);
                }
                tableModel.fireTableDataChanged();
                log.trace(tableModel.getRowCount());
            }
            repaint();
        }

    }
    class ForcedListSelectionModel extends DefaultListSelectionModel {
        public ForcedListSelectionModel () {
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }

        @Override
        public void clearSelection() {
        }

        @Override
        public void removeSelectionInterval(int index0, int index1) {
        }

    }
    public class NonEditableModel extends DefaultTableModel {

        NonEditableModel(Object[][] data, Object[] columnNames) {
            super(data, columnNames);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

}
