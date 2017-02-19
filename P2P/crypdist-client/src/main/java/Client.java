
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by od on 17.02.2017.
 */
public class Client extends Thread {

    private String swAdr;
    private int swPort;

    private int serverPort;
    private int heartBeatPort;
    private ServerSocket serverSocket;
    private ServerSocket heartBeatSocket;

    public Client (String swAdr, int swPort, int serverPort, int heartBeatPort) throws IOException {
        this.heartBeatPort = heartBeatPort;
        this.serverPort = serverPort;
        this.swAdr = swAdr;
        this.swPort = swPort;

        System.out.println("HB is opening from " + heartBeatPort);
        heartBeatSocket = new ServerSocket(heartBeatPort);
        Socket serverConnection = new Socket(swAdr,swPort);

        DataOutputStream out = new DataOutputStream(serverConnection.getOutputStream());
        out.writeInt(serverPort);
        out.writeInt(heartBeatPort);

        serverConnection.close();
    }

    public void run() {

        while (true) {
            try {
                Socket hb = heartBeatSocket.accept();

                DataInputStream in = new DataInputStream(hb.getInputStream());
                int flag = in.readInt();
                System.out.println("Flag: " + flag);

                int port = in.readInt();

                System.out.println("Port: " + port);
                Socket serverConnection = new Socket(swAdr,port);

                System.out.println("Sending to " + swAdr + " " + port);
                DataOutputStream out = new DataOutputStream(serverConnection.getOutputStream());
                out.writeInt(0);
                out.writeUTF(InetAddress.getLoopbackAddress().getHostAddress());
                out.flush();
                serverConnection.close();
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
