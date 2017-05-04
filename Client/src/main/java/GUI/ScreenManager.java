package GUI;

import Blockchain.Block;
import Blockchain.Blockchain;
import Blockchain.Transaction;
import Util.CrypDist;
import sun.awt.X11.Screen;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by gizem on 06.04.2017.
 */
public class ScreenManager extends JFrame{

    private CrypDist crypDist;
    JPanel currentView;

    private final int dimensionX = 1000;
    private final int dimensionY = 600;
    private boolean authenticated = false;

    public ScreenManager(CrypDist crypDist) {
        this.crypDist = crypDist;
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
        setSize(new Dimension(dimensionX,dimensionY));
        getContentPane().setBackground(Color.white);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void setCurrentView(JPanel panel) {
        if(currentView == null) {
            add(panel);
        }
        else {
            remove(currentView);
            add(panel);
        }
        currentView = panel;
        currentView.repaint();
        setVisible(true);

    }

    public JPanel getCurrentView() {
        return currentView;
    }

    public String getBlockContent(String blockId) {
//        Block block = crypDist.blockchainManager.getBlock(blockId);
//        ArrayList<Transaction> transactions = block.getTransactions();
//        String result = "";
//        for (int i = 0; i < transactions.size(); i++)
//        {
//            Transaction transaction = transactions.get(i);
//            result += transaction.getDataSummary() + "\n";
//        }
        String result = "";
        return result;
    }

    public void showLogin() {
        JPanel p = new JPanel(new BorderLayout(5,5));

        JPanel labels = new JPanel(new GridLayout(0,1,2,2));
        labels.add(new JLabel("Id", SwingConstants.RIGHT));
        labels.add(new JLabel("Password", SwingConstants.RIGHT));
        p.add(labels, BorderLayout.WEST);

        JPanel controls = new JPanel(new GridLayout(0,1,2,2));
        JTextField username = new JTextField();
        controls.add(username);
        JPasswordField password = new JPasswordField();
        password.addAncestorListener(new RequestFocusListener(false));
        controls.add(password);
        p.add(controls, BorderLayout.CENTER);

        String userName = null;
        boolean validUser = false;
        int result = JOptionPane.CANCEL_OPTION;

        do {
            result = JOptionPane.showConfirmDialog(
                    this, p, "Log In", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            switch (result) {
                case JOptionPane.OK_OPTION:
                    // Verify user details
                    if(authenticate(username.getText(),password.hashCode()))
                        validUser = true;
                    if (!validUser) {
                        JOptionPane.showMessageDialog(this, "Invalid username/password", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    break;
            }
        } while (!validUser && result != JOptionPane.CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            if (validUser) {
                JOptionPane.showMessageDialog(this, "You are logged into system " + username.getText(), "Welcome", JOptionPane.INFORMATION_MESSAGE);
                authenticated = true;
            }
        }
    }

    private boolean authenticate(String text, int i) {
        return true;
    }

    public boolean getAuthenticated() {
        //return crypDist.isAuthenticated();
        return true;
    }

    public String[][] getBlockList() {
        // TODO
        String[][] blockList = {
                {"#id1", "2017-04-11T18:46:07+00:00 "},
                {"#id2", "2016-04-11T18:46:07+00:00 "},
                {"#id3", "2015-04-11T18:46:07+00:00 "},
                {"#id4", "2014-04-11T18:46:07+00:00 "},
                {"#id5", "2013-04-11T18:46:07+00:00 "}
        };
//        Blockchain blockchain = crypDist.blockchainManager.getBlockchain();
//        Set<String> keySet = blockchain.getKeySet();
//        String[][] blockList = new String[keySet.size()][2];
//        Iterator<String> iterator = keySet.iterator();
//        int index = 0;
//
//        while (iterator.hasNext())
//        {
//            Block block = blockchain.getBlock(iterator.next());
//            blockList[index][0] = block.getHash();
//            blockList[index++][1] = block.getTimestamp() + "";
//        }
        return blockList;
    }

    public void abortUpload() throws InterruptedException {
         /* cancel upload or update data  */
        // TODO Interrupt upload
        TimeUnit.SECONDS.sleep(1);
    }

    public void uploadData(String filePath, String dataSummary) throws InterruptedException {
        /* upload data  */
//        try {
//            crypDist.blockchainManager.uploadFile(filePath, dataSummary);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        TimeUnit.SECONDS.sleep(5);
    }

    public void updateData(String text, String blockId) throws InterruptedException {
        /* update data  */
        // TODO
        TimeUnit.SECONDS.sleep(5);
    }

    public void showDownload(String s) {

        final JDialog dlg = new JDialog(this, "Cryp Dist", true);
        dlg.setLayout(new GridLayout(3,0));

        final JLabel label = new JLabel("Downloading...");
        dlg.add(label);

        final JProgressBar progressBar = new JProgressBar();
        progressBar.setBorder( new EmptyBorder( 10,10,10,10 ) );
        progressBar.setIndeterminate(true);
        dlg.add(progressBar);

        final GlossyButton cancel = new GlossyButton("Cancel");
        final JPanel bottomP = new JPanel();

        final Thread t = new Thread(new Runnable() {
            public void run() {
                dlg.setVisible(true);
            }
        });

        final Thread download = new Thread(new Runnable() {
            public void run()
            {
                /*
                * download data
                * */
                try {
                    TimeUnit.SECONDS.sleep(5);
                    t.interrupt();
                    label.setText("Data downloaded..");
                    progressBar.setEnabled(false);
                    progressBar.setVisible(false);
                    bottomP.remove(cancel);
                    GlossyButton ok = new GlossyButton("OK");
                    ok.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            dlg.dispose();
                        }
                    } );
                    bottomP.add(ok);
                    ok.repaint();
                    dlg.repaint();
                    dlg.setVisible(true);
                } catch (InterruptedException e) {
                    cancelDownload();
                    dlg.dispose();
                }

            }
        });

        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                download.interrupt();
                t.interrupt();
            }
        } );
        bottomP.add(cancel);
        cancel.setSize(100,80);
        dlg.add(bottomP);
        dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dlg.setSize(600, 100);
        dlg.setLocationRelativeTo(this);


        t.start();
        download.start();
    }

    private void cancelDownload() {
        // TODO interrupt download
    }

    public HashMap<String, ArrayList<Transaction>> query(String text) {
        // TODO query();
        HashMap<String, ArrayList<Transaction>> queryResults = new HashMap<>();
//        ArrayList<String> arr1 = new ArrayList<>();
//        arr1.add("A");
//        arr1.add("B");
//        ArrayList<String> arr2 = new ArrayList<>();
//        arr1.add("C");
//        arr1.add("D");
//        queryResults.put("Hash1", arr1);
//        queryResults.put("Hash2", arr2);

        Blockchain blockchain = crypDist.blockchainManager.getBlockchain();
        Set<String> keySet = blockchain.getKeySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext())
        {
            Block block = blockchain.getBlock(iterator.next());
            ArrayList<Transaction> transactions = block.getTransactions();
            int index = 0;
            ArrayList<Transaction> selected = new ArrayList<>();
            for (int i = 0; i < transactions.size(); i++)
            {
                String summary = transactions.get(i).getDataSummary();
                if (summary.contains(text))
                    selected.add(transactions.get(i));
            }
            if (selected.size() > 0)
                queryResults.put(block.getHash(), selected);
        }
        return queryResults;
    }

    public boolean isPathExist(String text) {
        File file = new File(text);
        return file.exists();
    }

    // TODO remove
    public static void main(String[] args)
    {
        CrypDist crypDist = null;
        ScreenManager screenManager = new ScreenManager(crypDist);
        screenManager.setCurrentView(new MainScreen(screenManager));
    }
}
