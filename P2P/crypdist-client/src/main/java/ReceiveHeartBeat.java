import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Created by od on 3.03.2017.
 */
public class ReceiveHeartBeat implements Runnable {

    private ServerSocket heartBeatSocket;

    public ReceiveHeartBeat(ServerSocket heartBeatSocket) {
        this.heartBeatSocket = heartBeatSocket;
    }

    public void run() {
        while (true) {
            try {
                Socket hb = heartBeatSocket.accept();
                System.out.println("Hb recieved.");
                new Thread(() -> {
                    try {
                        DataInputStream in = new DataInputStream(hb.getInputStream());
                        int flag = in.readInt();
                        System.out.println("Flag: " + flag);

                        if(flag == 0) {
                            DataOutputStream out = new DataOutputStream(hb.getOutputStream());
                            out.writeInt(1);
                            out.flush();
                        }

                        hb.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
            catch (SocketTimeoutException s) {
                System.out.println("Socket timed out!");
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
