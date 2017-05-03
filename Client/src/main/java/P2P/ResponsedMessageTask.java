package P2P;

import Util.Config;
import org.apache.log4j.Logger;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.Callable;

/**
 * Created by od on 27.04.2017.
 */
public class ResponsedMessageTask implements Callable<String> {

    private Peer p;
    private String msg;
    private Logger log = Client.log;

    public ResponsedMessageTask(Peer p, String msg) {
        this.p = p;
        this.msg = msg;
    }

    @Override
    public String call() throws Exception {

        int trials = 0;
        while(trials < Config.MESSAGE_MAX_TRIALS) {

            try {
                log.trace(p.getPeerServerPort());
                log.trace(p.getAddress());
                Socket messagedClient = new Socket(p.getAddress(), p.getPeerServerPort());
                messagedClient.setSoTimeout(1000);
                ObjectOutputStream out = new ObjectOutputStream(messagedClient.getOutputStream());
                out.writeInt(Config.MESSAGE_OUTGOING_RESPONSE);
                out.writeUTF(msg);
                out.flush();

                ObjectInputStream in = new ObjectInputStream(new DataInputStream(messagedClient.getInputStream()));
                int ack = in.readInt();

                if (ack == Config.MESSAGE_ACK) {
                    String response = in.readUTF();
                    return response;
                } else {
                    log.info("Non flag read");
                }
                messagedClient.close();

            } catch (IOException e) {
                log.info("EXCEPTIOON\n\n\n");
                log.info(e);
            }
            trials++;
        }
        log.error("Message cannot be sent after " + Config.MESSAGE_MAX_TRIALS + " trials");
        log.trace(msg);
        return null;
    }
}
