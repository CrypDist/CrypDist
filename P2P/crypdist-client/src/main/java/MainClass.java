
/**
 * Created by od on 16.02.2017.
 */
public class MainClass {

    public static void main (String[] args) {
        try {
            Thread t = new Client("localhost", 4142, 4533, 4532);
            t.start();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
