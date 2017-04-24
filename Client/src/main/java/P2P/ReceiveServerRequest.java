package P2P;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Created by od on 3.03.2017.
 */
public class ReceiveServerRequest extends Thread {

    private Client client;
    ServerSocket serverSocket;

    public ReceiveServerRequest(Client client) {
        this.client = client;
    }

    public void run() {

        try {
             serverSocket = new ServerSocket(client.getServerPort());
        } catch (IOException e) {
            System.err.println("Cannot open the server socket.");
            return;
        }

        while (true) {
            try {
                Socket server = serverSocket.accept();

                new Thread(() -> {
                    try {
                        System.out.println("Server request incoming.");

                        ObjectInputStream in = new ObjectInputStream(new DataInputStream(server.getInputStream()));
                        int flag = in.readInt();
                        String str = in.readUTF();

                        str = server.getInetAddress().toString() + "////" + str;

                        System.out.println("Client is notifying with " + flag  + " | " + str);
                        client.change();
                        client.notifyObservers(str);

                        server.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if(server != null && !server.isClosed())
                            try {
                                server.close();
                            } catch (Exception e) {

                            }
                    }

                }).start();

            }
            catch (SocketTimeoutException s) {
                System.err.println("Server socket timed out!");
            } catch (IOException e) {
                System.err.println("IOException while receiving server request!");
                e.printStackTrace();
            }
        }
    }
}