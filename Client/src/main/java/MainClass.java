import GUI.MainScreen;
import GUI.ScreenManager;
import Util.CrypDist;

import javax.swing.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by od on 17.04.2017.
 */
public class MainClass {

    public static void main (String [] args) {

        try {
            Socket conn = new Socket();
            conn.bind(new InetSocketAddress("localhost", 8080));
            conn.connect(new InetSocketAddress());
            System.out.println("BOOOO");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Property p = new Property();
        CrypDist c = new CrypDist();
        System.out.println("HEllo");

        //ScreenManager screenManager = new ScreenManager(c);
        //screenManager.setCurrentView(new MainScreen(screenManager));

        Scanner scan = new Scanner(System.in);
        while(true) {
            System.out.print(">>");
            String input = scan.nextLine();
            String[] inputSplitted = input.split(" /// ");
            String x = inputSplitted[0];
            String name = null;
            String path = null;
            if (inputSplitted.length > 1) {
                name = inputSplitted[1];
                if (inputSplitted.length > 2)
                    path = inputSplitted[2];
            }
            switch (x) {
                case "upload":
                    if (inputSplitted.length == 1)
                    {
                        try {
                            c.blockchainManager.uploadFile("SelaminAleykum/merhaba", "Summary");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        try {
                            c.blockchainManager.uploadFile(name, "Summary");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case "download":
                    c.blockchainManager.downloadFile(name, path);
                    break;
                case "save":
                    c.blockchainManager.saveBlockchain();
                    break;
                case "fetch":
                    c.blockchainManager.buildBlockchain();
                    break;
            }
        }
    }
}
