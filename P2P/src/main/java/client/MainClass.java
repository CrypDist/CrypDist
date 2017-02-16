package client;

/**
 * Created by od on 16.02.2017.
 */
public class MainClass {

    public static void main (String[] args) {
        Peer p = new Peer("localhost",4142);

        System.out.println(p.receiveMessage());
        System.out.println("Ex");
    }
}
