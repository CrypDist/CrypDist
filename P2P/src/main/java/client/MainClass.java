package client;

import server.Server;

import java.io.IOException;

/**
 * Created by od on 16.02.2017.
 */
public class MainClass {

    public static void main (String[] args) {
        try {
            Thread t = new Client("localhost", 4142, 4544, 4545);
            t.start();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
