import java.io.IOException;

/**
 * Created by od on 16.02.2017.
 */
public class Main {

    public static void main (String[] args) {
        try {
            Thread t = new Server(4142);
            t.start();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

}
