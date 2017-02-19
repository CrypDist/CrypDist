import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by od on 16.02.2017.
 */
public class Peer {
    private String address;
    private int port;
    private Socket socket;


    public Peer(String address, int port) {
        this.address = address;
        this.port = port;

        try {
            socket = new Socket(address,port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void sendMessage(int message) {
        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeInt(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String receiveMessage() {
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            return in.readUTF();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "None.";
    }

    public int receiveMessage2() {
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            return in.readInt();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return -1;
    }
    public void close () {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
