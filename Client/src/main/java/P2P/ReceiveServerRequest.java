package P2P;

import org.apache.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Created by od on 3.03.2017.
 */
public class ReceiveServerRequest extends Thread {

    private static Logger log = Client.log;

    private Client client;
    ServerSocket serverSocket;

    public ReceiveServerRequest(Client client) {
        this.client = client;
    }

    public void run() {

        try {
             serverSocket = new ServerSocket(client.getServerPort());
        } catch (IOException e) {
            log.fatal("Cannot open the server socket.");
            log.trace(e);
            return;
        }

        while (true) {
            try {
                Socket server = serverSocket.accept();

                new Thread(() -> {
                    try {
                        log.trace("Server request incoming.");

                        ObjectInputStream in = new ObjectInputStream(new DataInputStream(server.getInputStream()));
                        int flag = in.readInt();
                        String str = in.readUTF();

                        str = server.getInetAddress().toString() + "////" + str;

                        ObjectOutputStream out = new ObjectOutputStream(new DataOutputStream(server.getOutputStream()));
                        out.writeInt(900);
                        out.flush();

                        log.trace("Client is notifying with " + flag  + " | " + str);
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
                log.error("Server socket timed out!");
                log.trace(s);
            } catch (IOException e) {
                log.error("IOException while receiving server request!");
                log.trace(e);
            }
        }
    }
}