
/**
 * Created by od on 16.02.2017.
 */
public class MainClass {

    public static void main (String[] args) {

            Client c = new Client("207.154.219.184", Integer.parseInt(args[0]), Integer.parseInt(args[1])
                    , Integer.parseInt(args[2]));
        try {
            Thread.sleep(10000);
            System.out.println("Sending message");
            Thread.sleep(1000);

            c.broadCastMessage("Selamin Aleykum agalar.");
        } catch (Exception e) {
            return;
        }
    }
}
