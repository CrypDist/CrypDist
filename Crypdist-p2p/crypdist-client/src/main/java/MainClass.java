
/**
 * Created by od on 16.02.2017.
 */
public class MainClass {

    public static void main (String[] args) {
        try {
            Thread t = new Client("207.154.213.131", Integer.parseInt(args[0]), Integer.parseInt(args[1])
                    , Integer.parseInt(args[2]));
            t.start();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
