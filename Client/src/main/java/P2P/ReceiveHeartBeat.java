package P2P;

import Util.Config;
import org.apache.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Created by od on 3.03.2017.
 */
public class ReceiveHeartBeat extends Thread {

    private static Logger log = Client.log;

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


                if(flag != Config.HEARTBEAT_FLAG_SERVER && flag != Config.HEARTBEAT_FLAG_CLIENT) {
                    hb.close();
                    return;
                }


                if(flag == Config.HEARTBEAT_FLAG_CLIENT) {
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
                        Peer x = new Peer(addr,hbPort,swPort);
                        log.trace("New peer added.");
                        client.peerList.put(x,0);
                    }
                }

                DataOutputStream out = new DataOutputStream(hb.getOutputStream());
                out.writeInt(Config.HEARTBEAT_ACK);
                out.flush();

                hb.close();
            }
            catch (SocketTimeoutException s) {
                log.trace("Server socket timed out!");
                log.trace(s);
            } catch (IOException e) {
                log.trace("IOException while receiving server request!");
                log.trace(e);
            }
        }
    }
}
