import Util.CrypDist;

import java.util.Scanner;

/**
 * Created by od on 17.04.2017.
 */
public class MainClass {

    public static void main (String [] args) {

        CrypDist c = new CrypDist(args[0], Integer.parseInt(args[1]),Integer.parseInt(args[2]),Integer.parseInt(args[3]));

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
                        c.blockchainManager.uploadFile("SelaminAleykum/merhaba", "Summary");
                    }
                    else {
                        c.blockchainManager.uploadFile(name, "Summary");
                    }
                    break;
                case "download":
                    c.blockchainManager.downloadFile(name, path);
                case "save":
                    c.blockchainManager.saveBlockchain();
                case "fetch":
                    c.blockchainManager.buildBlockchain();
            }
        }
    }
}
