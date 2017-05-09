package GUI;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.NoSuchAlgorithmException;
import java.util.Vector;

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
    static Logger log = Logger.getLogger("GUI");

    // blocklist numberOfBlocksX2 blocklist[i][0] id of ith block, blocklist[i][1] timestamp of ith block
    public  MainScreen(ScreenManager controller)
    {
        this.controller = controller;
        String[][] blocklist = new String[0][];
        try {
            blocklist = controller.getBlockList();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        setSize(new Dimension(1000,600));

        JPanel buttonsPanel = new JPanel(new GridLayout(0,1));
        upload = new GlossyButton("Upload");
        query = new GlossyButton("Query");
        refresh = new GlossyButton("Refresh");

        if(Config.USER_NAME == "")
            upload.setEnabled(false);
        else
            upload.setEnabled(true);

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
        JTextArea content = new JTextArea();
        content.setEditable(false);
        JScrollPane contentScroll = new JScrollPane(content);
        blockContent.add(contentScroll);

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
        String content = controller.getBlockContent(blockId);
        JViewport viewport = ((JScrollPane)(blockContent.getComponents()[0])).getViewport();
        JTextArea area = (JTextArea)viewport.getView();
        area.setText(content);
        blockContent.repaint();
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
