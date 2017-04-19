import java.util.Scanner;

/**
 * Created by od on 17.04.2017.
 */
public class MainClass {

    public static void main (String [] args) {

        System.out.println("anan" + args[0]);
        CrypDist c = new CrypDist(args[0], Integer.parseInt(args[1]),Integer.parseInt(args[2]),Integer.parseInt(args[3]));

        Scanner scan = new Scanner(System.in);
        while(true) {
            System.out.print(">>");
            String x = scan.nextLine();

            switch (x) {
                case "upload":
                    c.blockchainManager.uploadFile("SelaminAleykum/Merhaba");
                    break;

            }


        }
    }
}
