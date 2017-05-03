import Util.CrypDist;

import java.util.Scanner;

/**
 * Created by od on 17.04.2017.
 */
public class MainClass {

    public static void main (String [] args) {

        Property p = new Property();
        CrypDist c = new CrypDist();

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
