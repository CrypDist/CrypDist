import GUI.MainScreen;
import GUI.ScreenManager;
import Util.CrypDist;
import Util.CustomAppender;

import java.util.Scanner;

/**
 * Created by od on 17.04.2017.
 */
public class MainClass {

    public static void main (String [] args)
    {
        Property p = new Property();
        int crypType = 0;
        if (args.length == 1)
            crypType = 1;
        if (crypType == 0) {
            ScreenManager screenManager = new ScreenManager();
            screenManager.showLogin();
        }
        else {
            CrypDist c = new CrypDist();
            Scanner scan = new Scanner(System.in);
            System.out.println(CustomAppender.getMessages());
            while (true) {
                System.out.print(">>");
                String input = scan.nextLine();
                String[] inputSplitted = input.split(" /// ");
                String x = inputSplitted[0];
                String name = null;
                String path = null;
                String summary = null;
                if (inputSplitted.length > 1) {
                    name = inputSplitted[1];
                    if (inputSplitted.length > 2) {
                        path = inputSplitted[2];
                    }
                }
                switch (x) {
                    case "upload":
                        CustomAppender.clear();
                        if (inputSplitted.length == 1) {
                            try {
                                c.getBlockchainManager().uploadFile("SelaminAleykum/merhaba", "Summary");
                            } catch (Exception e) {
                            }
                        } else {
                            try {
                                c.getBlockchainManager().uploadFile(name, path);
                            } catch (Exception e) {
                            }
                        }
                        System.out.println(CustomAppender.getMessages());
                        break;
                    case "download":
                        CustomAppender.clear();
                        c.getBlockchainManager().downloadFile(name, path);
                        System.out.println(CustomAppender.getMessages());
                        break;
                    case "save":
                        CustomAppender.clear();
                        c.getBlockchainManager().saveBlockchain();
                        System.out.println(CustomAppender.getMessages());
                        break;
                    case "fetch":
                        CustomAppender.clear();
                        c.getBlockchainManager().buildBlockchain();
                        System.out.println(CustomAppender.getMessages());
                        break;
                    case "status":
                        System.out.println(CustomAppender.getMessages());
                        break;
                }
            }
        }
    }
}
