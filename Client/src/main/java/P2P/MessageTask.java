package P2P;

import Util.Config;
import org.apache.log4j.Logger;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by od on 27.04.2017.
 */
public class MessageTask implements Runnable {

    private Peer p;
    private String msg;
    private Logger log = Client.log;

    public MessageTask(Peer p, String msg) {
        this.p = p;
        this.msg = msg;
    }

    @Override
    public void run() {

        int trials = 0;
        while(trials < Config.MESSAGE_MAX_TRIALS) {

            try {
                log.trace(p.getPeerServerPort());
                log.trace(p.getAddress());
                Socket messagedClient = new Socket(p.getAddress(), p.getPeerServerPort());
                ObjectOutputStream out = new ObjectOutputStream(messagedClient.getOutputStream());
                out.writeInt(Config.MESSAGE_OUTGOING);
                out.writeUTF(msg);
                out.flush();

                ObjectInputStream in = new ObjectInputStream(new DataInputStream(messagedClient.getInputStream()));
                int ack = in.readInt();
                messagedClient.close();
                if (ack == Config.MESSAGE_ACK) {
                    return;
                }

            } catch (IOException e) {

            }
            trials++;
        }
        log.error("Message cannot be sent after " + Config.MESSAGE_MAX_TRIALS + " trials");
        log.trace(msg);
    }
}
