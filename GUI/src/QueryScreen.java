import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by gizem on 06.04.2017.
 */
public class QueryScreen extends JPanel {

    JTextArea query;
    JTextArea results;
    GlossyButton run;
    GlossyButton back;
    ScreenManager controller;

    public QueryScreen(ScreenManager controller) {
        this.controller = controller;
        setSize((new Dimension(1000,600)));
        setBackground(Color.white);

        query = new JTextArea();
        results = new JTextArea();
        run = new GlossyButton("Run");
        back = new GlossyButton("Back");

        results.setEditable(false);

        ButtonListener l = new ButtonListener();
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

        gbc.ipady = getHeight()/2 - 125;
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

        repaint();
        setVisible(true);
    }
    class ButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {

            if(e.getSource() == run) {
                back.setEnabled(false);
                // TODO: Check if query statement is correct
                if(query.getText().equals("")) {
                    JOptionPane.showMessageDialog(QueryScreen.this, "Please enter a query statement!", "Warning",
                            JOptionPane.WARNING_MESSAGE);
                }
                String result = controller.query(query.getText());
                results.setText(result);
                back.setEnabled(true);
            }
            else {  // back
                controller.setCurrentView(new MainScreen(controller));
                controller.setSize((new Dimension(1000,600)));
            }
            repaint();
        }

    }
}
