package GUI;

import Blockchain.Transaction;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by gizem on 06.04.2017.
 */
public class QueryScreen extends JPanel {

    JTextField query;
    GlossyButton run;
    GlossyButton back;
    ScreenManager controller;
    JTable results;
    DefaultTableModel resultsModel;
    ArrayList<Transaction> currTransactions;
    JPopupMenu popupMenu;
    JProgressBar progressBar;

    public QueryScreen(ScreenManager controller) {
        this.controller = controller;
        setSize((new Dimension(1000,600)));
        setBackground(Color.white);

        query = new JTextField();
        resultsModel = new DefaultTableModel() {
            public boolean isCellEditable(int row, int col)
            {
                return false;
            }
        };
        resultsModel.addColumn("");
        resultsModel.addColumn("");
        results = new JTable(resultsModel);
        results.addMouseListener(new TableListener());
        currTransactions = new ArrayList<>();
        currTransactions.add(null);
        progressBar = new JProgressBar();
        progressBar.setBackground(Color.white);

        query.setFont(new Font("Arial", Font.BOLD,20));

        DefaultTableCellRenderer moreRenderer = new DefaultTableCellRenderer() {
            Font font = new Font("Arial",Font.BOLD,results.getFont().getSize());

            @Override
            public Component getTableCellRendererComponent(JTable table,
                                                           Object value, boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                        row, column);
                setFont(font);
                setForeground(Color.BLUE);
                return this;
            }

        };
        TableColumnModel jTableColumnModel = results.getColumnModel();
        jTableColumnModel.getColumn(1).setMaxWidth(50);
        jTableColumnModel.getColumn(1).setCellRenderer(moreRenderer);

        run = new GlossyButton("Run");
        back = new GlossyButton("Back");

        ButtonListener l = new ButtonListener();
        query.addActionListener(l);
        run.addActionListener(l);
        back.addActionListener(l);

        JScrollPane queryScroll = new JScrollPane(query);
        queryScroll.setBackground(Color.white);
        JScrollPane resultsScroll = new JScrollPane(results);
        resultsScroll.setBackground(Color.white);

        Border border = BorderFactory.createLineBorder(Color.GRAY, 3);

        queryScroll.setBorder(BorderFactory.createCompoundBorder(border,
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        resultsScroll.setBorder(BorderFactory.createCompoundBorder(border,
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        run.setBorder(BorderFactory.createCompoundBorder(border,
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        back.setBorder(BorderFactory.createCompoundBorder(border,
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.ipady = getHeight()/2 - 250;
        gbc.ipadx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 0.5;
        gbc.gridy = 0;
        gbc.gridwidth = 5;
        gbc.insets = new Insets(10,10,5,10);
        add(queryScroll,gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipady = 0;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5,10,5,10);
        add(run,gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 0;
        gbc.ipady = getHeight()/2 - 125;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(10,10,5,10);
        gbc.gridwidth = 5;
        add(resultsScroll,gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipady = 0;
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        gbc.insets = new Insets(5,10,5,10);
        gbc.gridwidth = 1;
        add(back,gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipady = 0;
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        gbc.insets = new Insets(5,10,50,10);
        gbc.gridwidth = 5;
        add(progressBar,gbc);

        repaint();
        setVisible(true);
        popupMenu = new JPopupMenu();
    }

    class ButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {

            if(e.getSource() == run || e.getSource() == query)
            {
                back.setEnabled(false);
                // TODO: Check if query statement is correct
                if(query.getText().equals("")) {
                    JOptionPane.showMessageDialog(QueryScreen.this, "Please enter a query statement!", "Warning",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                resultsModel.setRowCount(0);
                currTransactions.clear();

                HashMap<String, ArrayList<Transaction>> queryResults = controller.query(query.getText());
                Set<String> keySet = queryResults.keySet();
                Iterator<String> iterator = keySet.iterator();
                while (iterator.hasNext())
                {
                    String key = iterator.next();
                    ArrayList<Transaction> transactions = queryResults.get(key);

                    for (int i = 0; i < transactions.size(); i++)
                    {
                        Object[] rowData = {transactions.get(i).getDataSummary()};
                        resultsModel.addRow(rowData);
                        resultsModel.setValueAt("More", resultsModel.getRowCount()-1,1);
                        resultsModel.fireTableDataChanged();
                        currTransactions.add(transactions.get(i));
                    }
                }
                back.setEnabled(true);
            }
            else {  // back
                controller.setCurrentView(new MainScreen(controller));
                controller.setSize((new Dimension(1000,600)));
            }
            repaint();
        }
    }

    class TableListener extends MouseAdapter
    {
        public void mouseClicked(MouseEvent event)
        {
            popupMenu.setVisible(false);
            results.clearSelection();
            if(results.columnAtPoint(event.getPoint()) == 1) {
                int row = results.rowAtPoint(event.getPoint());
                if(currTransactions.get(row) != null ) {
                    JOptionPane.showMessageDialog(QueryScreen.this, "File name: " + currTransactions.get(row).getFileName()
                            + "\nUser name: " + currTransactions.get(row).getSignature());
                }

            }
            if (SwingUtilities.isRightMouseButton(event))
            {
                int row = results.rowAtPoint(event.getPoint());
                results.setRowSelectionInterval(row, row);
                if (row >= 0 && row < currTransactions.size())
                {
                    popupMenu = new JPopupMenu();
                    JMenuItem download = new JMenuItem("Download");
                    JMenuItem update = new JMenuItem("Update");
                    popupMenu.add(download);
                    popupMenu.add(update);
                    popupMenu.setLocation(event.getXOnScreen(), event.getYOnScreen());
                    popupMenu.setVisible(true);
                    download.addActionListener(e -> {
                        update.setSelected(false);
                        String filename = currTransactions.get(row).getFileName();
                        JFileChooser fileChooser = new JFileChooser();
                        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                        int returnVal = fileChooser.showOpenDialog(QueryScreen.this);

                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            String path = fileChooser.getSelectedFile().getAbsolutePath();
                            path = path + "/" + filename ;
                            filename += currTransactions.get(row).getVersion();
                            popupMenu.setVisible(false);
                            controller.showDownload(filename, path);
                        }
                        else
                        {
                            download.setSelected(false);
                            update.setSelected(false);
                            popupMenu.setVisible(false);
                        }
                    });
                    update.addActionListener(e -> {
                        download.setSelected(false);
                        JFileChooser fileChooser = new JFileChooser();
                        int returnVal = fileChooser.showOpenDialog(QueryScreen.this);

                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            progressBar.setVisible(true);
                            progressBar.setIndeterminate(true);
                            String path = fileChooser.getSelectedFile().getAbsolutePath();
                            String fileName = fileChooser.getSelectedFile().getName();
                            try {
                                controller.updateData(currTransactions.get(row), path, fileName);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                            progressBar.setVisible(false);
                            progressBar.setIndeterminate(false);
                            popupMenu.setVisible(false);
                        }
                        else
                        {
                            download.setSelected(false);
                            update.setSelected(false);
                            popupMenu.setVisible(false);
                        }
                    });
                }
            }
        }
    }
}
