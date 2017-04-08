import com.sun.org.apache.xpath.internal.SourceTree;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
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

                DataInputStream in = new DataInputStream(hb.getInputStream());
                int flag = in.readInt();


                if(flag != 100 && flag != 101) {
                    hb.close();
                    return;
                }


                if(flag == 101) {
                    int hbPort = in.readInt();
                    int swPort = in.readInt();

                    InetAddress addr = hb.getInetAddress();

                    boolean b = true;
                    for(Peer p: client.peerList.keySet()) {
                        if (p.getAddress().equals(addr)) {
                            b = false;
                            break;
                        }
                    }
                    if (b) {
                        Peer x = new Peer(addr,swPort,hbPort);
                        System.out.println("New peer added.");
                        client.peerList.put(x,0);
                    }
                }

                DataOutputStream out = new DataOutputStream(hb.getOutputStream());
                out.writeInt(102);
                out.flush();

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
