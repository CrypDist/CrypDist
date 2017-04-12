import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Created by gizem on 06.04.2017.
 */
public class DataUploadScreen extends JPanel implements ActionListener{

    JLabel label;
    GlossyButton upload;
    GlossyButton back;
    GlossyButton cancel;
    GlossyButton browse;
    JTextField pathField;
    JProgressBar progressBar;
    ScreenManager controller;

    public DataUploadScreen(ScreenManager controller) {
        this.controller = controller;
        setSize((new Dimension(600,300)));
        setBackground(Color.white);

        label = new JLabel("Select the file to be added:");
        upload = new GlossyButton("Upload");
        back = new GlossyButton("Back");
        cancel = new GlossyButton("Cancel");
        browse = new GlossyButton("Browse");
        pathField = new JTextField(30);
        progressBar = new JProgressBar();

        upload.addActionListener(this);
        back.addActionListener(this);
        cancel.addActionListener(this);
        browse.addActionListener(this);

        cancel.setEnabled(false);
        progressBar.setVisible(false);

        Border border = BorderFactory.createLineBorder(Color.GRAY, 1);
        label.setBorder(new EmptyBorder(10, 40, 10, 10));
        pathField.setFont(new Font("Arial", Font.LAYOUT_LEFT_TO_RIGHT,14));
        pathField.setBorder(border);

        // Create the layout structure
        GridLayout mainLayout = new GridLayout(5,0);
        this.setLayout(mainLayout);

        JPanel bottom = new JPanel();
        bottom.setBackground(Color.white);
        bottom.add(back);
        bottom.add(cancel);

        JPanel browsePanel = new JPanel(new FlowLayout(FlowLayout.LEFT,20,0));
        browsePanel.setBackground(Color.white);
        browsePanel.add(pathField);
        browsePanel.add(browse);
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
                    if(controller.isPathExist(pathField.getText())) {
                        pathField.setEditable(false);
                        browse.setEnabled(false);
                        back.setEnabled(false);
                        cancel.setEnabled(true);
                        progressBar.setVisible(true);
                        progressBar.setIndeterminate(true);

                        Runnable myrunnable = new Runnable() {
                            public void run() {

                                try {
                                    controller.uploadData(pathField.getText());
                                    pathField.setEditable(true);
                                    browse.setEnabled(true);
                                    back.setEnabled(true);
                                    cancel.setEnabled(false);
                                    progressBar.setVisible(false);
                                    progressBar.setIndeterminate(false);
                                    pathField.setText("");
                                } catch (InterruptedException e1) {
                                    e1.printStackTrace();
                                }
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
        else if(e.getSource() == cancel) {
            try {
                progressBar.setVisible(false);
                progressBar.setIndeterminate(false);
                cancel.setEnabled(false);
                controller.abortUpload();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            pathField.setEditable(true);
            browse.setEnabled(true);
            back.setEnabled(true);
            pathField.setText("");
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
