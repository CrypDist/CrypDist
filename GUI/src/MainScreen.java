import sun.applet.Main;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by gizem on 06.04.2017.
 */
public class MainScreen extends JPanel {
    JTable blockTable;
    JPanel blockContent;
    GlossyButton update;
    GlossyButton download;
    GlossyButton upload;
    GlossyButton query;
    GlossyButton authenticate;
    ScreenManager controller;

    // blocklist numberOfBlocksX2 blocklist[i][0] id of ith block, blocklist[i][1] timestamp of ith block
    public  MainScreen(ScreenManager controller) {

        this.controller = controller;
        String[][] blocklist = controller.getBlockList();
        setSize(new Dimension(1000,600));

        JPanel buttonsPanel = new JPanel(new GridLayout(0,1));
        update = new GlossyButton("Update");
        download = new GlossyButton("Download");
        upload = new GlossyButton("Upload");
        query = new GlossyButton("Query");
        authenticate = new GlossyButton("Authenticate");

        ButtonListener listener = new ButtonListener();
        update.addActionListener(listener);
        download.addActionListener(listener);
        upload.addActionListener(listener);
        query.addActionListener(listener);
        authenticate.addActionListener(listener);

        FlowLayout buttonsLayout = new FlowLayout();
        buttonsPanel.setLayout(buttonsLayout);
        buttonsPanel.setBackground(Color.white);
        buttonsPanel.add(update);
        buttonsPanel.add(download);
        buttonsPanel.add(upload);
        buttonsPanel.add(query);
        buttonsPanel.add(authenticate);
        buttonsPanel.setComponentOrientation(
                ComponentOrientation.LEFT_TO_RIGHT);

        JPanel blockPanel = new JPanel();
        GridLayout blockLayout = new GridLayout(0,2);
        blockPanel.setLayout(blockLayout);
        blockPanel.setBackground(Color.white);
        JScrollPane scrollPane = null;
        blockContent = new JPanel(new GridLayout(0,1));
        JTextArea content = new JTextArea();
        content.setEditable(false);
        JScrollPane contentScroll = new JScrollPane(content);
        blockContent.add(contentScroll);

        if(blocklist != null) {

            Object columnNames[] = {"blockId", "timestamp"};
            blockTable = new JTable(blocklist, columnNames);
            scrollPane = new JScrollPane(blockTable);
            add(scrollPane);
            blockTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
                public void valueChanged(ListSelectionEvent event) {
                    if (blockTable.getSelectedRow() > -1) {
                        // print first column value from selected row
                        System.out.println(blockTable.getValueAt(blockTable.getSelectedRow(), 0).toString());
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

            if (e.getSource() == update) {
                if(blockTable.getSelectionModel().isSelectionEmpty()) {
                    JOptionPane.showMessageDialog(MainScreen.this, "Please choose a block from the list", "Warning",
                            JOptionPane.WARNING_MESSAGE);
                }
                else {
                    controller.setCurrentView(new DataUpdateScreen(controller, blockTable.getValueAt(blockTable.getSelectedRow(), 0).toString()));
                    controller.setSize((new Dimension(600, 300)));
                }
            }
            else if(e.getSource() == download) {
                if(blockTable.getSelectionModel().isSelectionEmpty()) {
                    JOptionPane.showMessageDialog(MainScreen.this, "Please choose a block to download", "Warning",
                            JOptionPane.WARNING_MESSAGE);
                }
                else {
                    controller.showDownload(blockTable.getValueAt(blockTable.getSelectedRow(), 0).toString());
                }
            }
            else if(e.getSource() == upload) {
                controller.setCurrentView(new DataUploadScreen(controller));
                controller.setSize((new Dimension(600,300)));
            }
            else if(e.getSource() == query) {
                controller.setCurrentView(new QueryScreen(controller));
                controller.setSize((new Dimension(1000,600)));
            }
            else {  // authenticate
                controller.showLogin();
            }
            repaint();
        }

    }


}
