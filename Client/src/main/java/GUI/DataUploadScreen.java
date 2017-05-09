package GUI;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;

/**
 * Created by gizem on 06.04.2017.
 */
public class DataUploadScreen extends JPanel implements ActionListener{

    JLabel label;
    GlossyButton upload;
    GlossyButton back;
    GlossyButton browse;
    JTextField pathField;
    JTextField dataSummary;
    JProgressBar progressBar;
    ScreenManager controller;

    public DataUploadScreen(ScreenManager controller) {
        this.controller = controller;
        setSize((new Dimension(600,300)));
        setBackground(Color.white);

        label = new JLabel("Select the file to be added:");
        upload = new GlossyButton("Upload");
        back = new GlossyButton("Back");
        browse = new GlossyButton("Browse");
        pathField = new JTextField(30);
        dataSummary = new JTextField(30);
        progressBar = new JProgressBar();

        upload.addActionListener(this);
        back.addActionListener(this);
        browse.addActionListener(this);

        progressBar.setVisible(false);

        Border border = BorderFactory.createLineBorder(Color.GRAY, 1);
        label.setBorder(new EmptyBorder(10, 40, 10, 10));
        pathField.setFont(new Font("Arial", Font.LAYOUT_LEFT_TO_RIGHT,14));
        dataSummary.setFont(new Font("Arial", Font.LAYOUT_LEFT_TO_RIGHT,14));
        pathField.setBorder(border);

        pathField.setForeground(Color.GRAY);
        pathField.setText("Path");
        pathField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (pathField.getText().equals("Path")) {
                    pathField.setText("");
                    pathField.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (pathField.getText().isEmpty()) {
                    pathField.setForeground(Color.GRAY);
                    pathField.setText("Path");
                }
            }
        });

        dataSummary.setForeground(Color.GRAY);
        dataSummary.setText("Data summary");
        dataSummary.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (dataSummary.getText().equals("Data summary")) {
                    dataSummary.setText("");
                    dataSummary.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (dataSummary.getText().isEmpty()) {
                    dataSummary.setForeground(Color.GRAY);
                    dataSummary.setText("Data summary");
                }
            }
        });
        // Create the layout structure
        GridLayout mainLayout = new GridLayout(5,0);
        this.setLayout(mainLayout);

        JPanel bottom = new JPanel();
        bottom.setBackground(Color.white);
        bottom.add(back);

        JPanel browsePanel = new JPanel(new FlowLayout(FlowLayout.LEFT,20,0));
        browsePanel.setBackground(Color.white);
        browsePanel.add(pathField);
        browsePanel.add(browse);
        browsePanel.add(dataSummary);
        browsePanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 10));

        JPanel layer3 = new JPanel();
        layer3.setBackground(Color.white);
        layer3.add(upload);

        JPanel layer4 = new JPanel();
        layer4.setBackground(Color.white);
        progressBar.setBackground(Color.white);
        layer4.add(progressBar);

        add(label);
        add(browsePanel);
        add(layer3);
        add(layer4);
        add(bottom);

        repaint();
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == upload) {
            {
                if(pathField.getText().equals(""))
                    JOptionPane.showMessageDialog(this, "Please enter a valid path!", "Warning",
                            JOptionPane.WARNING_MESSAGE);
                else {
                    if (dataSummary.getText().isEmpty())
                    {
                        JOptionPane.showMessageDialog(DataUploadScreen.this,
                                "Please enter a data summary!", "Warning",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    else if(controller.isPathExist(pathField.getText())) {
                        pathField.setEditable(false);
                        browse.setEnabled(false);
                        back.setEnabled(false);
                        progressBar.setVisible(true);
                        progressBar.setIndeterminate(true);

                        Runnable myrunnable = () -> {
                            try {
                                String summary = dataSummary.getText();
                                controller.uploadData(pathField.getText(), summary);
                                pathField.setEditable(true);
                                browse.setEnabled(true);
                                back.setEnabled(true);
                                progressBar.setVisible(false);
                                progressBar.setIndeterminate(false);
                                pathField.setText("");
                                dataSummary.setText("");
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        };
                        new Thread(myrunnable).start();
                    }
                    else {
                        JOptionPane.showMessageDialog(this, "Please enter a valid path!", "Warning",
                                JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        }
        else if(e.getSource() == back) {
            controller.setCurrentView(new MainScreen(controller));
            controller.setSize((new Dimension(1000,600)));
        }
        else {  // Browse
            final JFileChooser fc = new JFileChooser();
            int returnVal = fc.showOpenDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                pathField.setText(file.getAbsolutePath());
            }
        }
        repaint();
    }
}
