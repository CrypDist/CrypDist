package P2P;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by od on 8.04.2017.
 */
public class PeerNotifier extends Thread {
    Peer p;
    int hb,sw;
    public PeerNotifier(Peer p,int hb, int sw) {
        this.p = p;
        this.hb = hb;
        this.sw = sw;
    }

    public void run() {
        try {
            Socket clientSocket = new Socket(p.getAddress(),p.getPeerHeartBeatPort());
            ObjectOutputStream out = new ObjectOutputStream(new DataOutputStream(clientSocket.getOutputStream()));
            out.writeInt(101);  //2 for new cons
            out.writeInt(hb);
            out.writeInt(sw);
            out.flush();

            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Cannot send the data to peer.");
        }
    }
}

