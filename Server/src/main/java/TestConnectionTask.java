import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.Callable;

/**
 * Created by od on 6.05.2017.
 */
public class TestConnectionTask implements Callable<Boolean> {

    private InetAddress address;
    private int port;

    public TestConnectionTask(InetAddress address, int port) {
        this.address = address;
        this.port = port;
    }

    public Boolean call(){

        int trials = 0;
        while(trials < Config.MESSAGE_TEST_TRIALS) {

            try {
                Socket test = new Socket(address,port);
                test.setSoTimeout(1000);
                ObjectOutputStream out = new ObjectOutputStream(test.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(new DataInputStream(test.getInputStream()));

                out.writeInt(Config.MESSAGE_SERVER_TEST);
                out.flush();

                int ack = in.readInt();
                test.close();
                if (ack == Config.MESSAGE_ACK) {
                    return true;
                }

            } catch (IOException e) {

            }
            trials++;
        }

        return false;

    }
}
