package GUI;

import Blockchain.Block;
import Blockchain.Blockchain;
import Blockchain.Transaction;
import Util.Config;
import Util.CrypDist;
import jdk.nashorn.internal.scripts.JO;
import sun.awt.ConstrainableGraphics;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by gizem on 06.04.2017.
 */
public class ScreenManager extends JFrame implements WindowListener{

    private CrypDist crypDist;
    JPanel currentView;
    private String userName;

    private final int dimensionX = 1000;
    private final int dimensionY = 600;

    public ScreenManager() {
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

    public String getBlockContent(String blockId) {
        Block block = crypDist.getBlockchainManager().getBlock(blockId);
        ArrayList<Transaction> transactions = block.getTransactions();
        String result = "";
        for (int i = 0; i < transactions.size(); i++)
        {
            Transaction transaction = transactions.get(i);
            result += transaction.getDataSummary() + "\n";
        }
        return result;
    }

    public void showLogin()
    {
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

        int result = JOptionPane.showConfirmDialog(
                    this, p, "Log In", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (result == JOptionPane.OK_OPTION)
        {
            Config.USER_NAME = username.getText();
            userName = username.getText();
            Config.USER_PASS = new String(password.getPassword());
            crypDist = new CrypDist(this);
            setCurrentView(new MainScreen(this));
        }
        else if (result == JOptionPane.CANCEL_OPTION)
        {
            Config.USER_NAME = "";
            Config.USER_PASS = "";
            crypDist = new CrypDist(this);
            setCurrentView(new MainScreen(this));
        }
    }

    public String getUserName()
    {
        return userName;
    }

    public boolean getAuthenticated() {
        return crypDist.isAuthenticated();
    }

    public String[][] getBlockList() throws NoSuchAlgorithmException {
        // TODO
//        String[][] blockList = {
//                {"#id1", "2017-04-11T18:46:07+00:00 "},
//                {"#id2", "2016-04-11T18:46:07+00:00 "},
//                {"#id3", "2015-04-11T18:46:07+00:00 "},
//                {"#id4", "2014-04-11T18:46:07+00:00 "},
//                {"#id5", "2013-04-11T18:46:07+00:00 "}
//        };
        crypDist.updateBlockchain();
        Blockchain blockchain = crypDist.getBlockchainManager().getBlockchain();
        Set<String> keySet = blockchain.getKeySet();

        ArrayList<String> keys = new ArrayList<>(keySet);
        Collections.sort(keys);
        String[][] blockList = new String[keySet.size()][2];
        int index = 0;


        for (String key : keys)
        {
            Block block = blockchain.getBlock(key);
            blockList[index][0] = block.getHash();
            blockList[index++][1] = block.getTimestamp() + "";
        }
        return blockList;
    }

    public void uploadData(String filePath, String dataSummary) throws InterruptedException {
        /* upload data  */
        try {
            crypDist.getBlockchainManager().uploadFile(filePath, dataSummary);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateData(Transaction transaction, String path, String fileName) throws InterruptedException {
        try {
            crypDist.getBlockchainManager().updateFile(transaction, path, fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showDownload(String filename, String path)
    {
        final JDialog dlg = new JDialog(this, "CrypDist", true);
        dlg.setLayout(new GridLayout(3,0));

        final JLabel label = new JLabel("Downloading...");
        dlg.add(label);

        final JProgressBar progressBar = new JProgressBar();
        progressBar.setBorder( new EmptyBorder( 10,10,10,10 ) );
        progressBar.setIndeterminate(true);
        dlg.add(progressBar);

        final GlossyButton cancel = new GlossyButton("Cancel");
        final JPanel bottomP = new JPanel();

        final Thread t = new Thread(() -> dlg.setVisible(true));

        final Thread download = new Thread(() -> {
            System.out.println("Name: " + filename);
            System.out.println("Path: " + path);
            crypDist.getBlockchainManager().downloadFile(filename, path);
            System.out.println(filename + " - " + path + " is downloaded");
            try {
                TimeUnit.SECONDS.sleep(5);
                t.interrupt();
                label.setText("Data downloaded..");
                progressBar.setEnabled(false);
                progressBar.setVisible(false);
                bottomP.remove(cancel);
                GlossyButton ok = new GlossyButton("OK");
                ok.addActionListener(e -> dlg.dispose());
                bottomP.add(ok);
                ok.repaint();
                dlg.repaint();
                dlg.setVisible(true);
            } catch (InterruptedException e) {
                dlg.dispose();
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

        Blockchain blockchain = crypDist.getBlockchainManager().getBlockchain();
        Set<String> keySet = blockchain.getKeySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext())
        {
            Block block = blockchain.getBlock(iterator.next());
            System.out.println(block != null);
            System.out.println(block.getTransactions() != null);

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

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e)
    {
        crypDist.getBlockchainManager().saveBlockchain();
    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }
}
