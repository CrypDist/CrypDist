import com.sun.org.apache.xpath.internal.SourceTree;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Created by od on 3.03.2017.
 */
public class ReceiveHeartBeat extends Thread {

    private Client client;

    public ReceiveHeartBeat(Client client) {
        this.client = client;
    }

    public void run() {
        while (true) {
            try {
                ServerSocket heartBeatSocket = new ServerSocket(client.getHeartBeatPort());
                Socket hb = heartBeatSocket.accept();
                heartBeatSocket.close();
                System.out.println("Hb recieved from: " + hb.getInetAddress());

                DataInputStream in = new DataInputStream(hb.getInputStream());
                int flag = in.readInt();

                if(flag == 0) {
                    DataOutputStream out = new DataOutputStream(hb.getOutputStream());
                    out.writeInt(1);
                    out.flush();
                }
                else {
                    System.err.println("Unknown heartbeat request/response.");
                }

                hb.close();
            }
            catch (SocketTimeoutException s) {
                System.err.println("Server socket timed out!");
            } catch (IOException e) {
                System.err.println("IOException while receiving heartbeat!");
                e.printStackTrace();
            }
        }
    }
}
