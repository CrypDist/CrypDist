import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by od on 16.02.2017.
 */
public class Peer implements Serializable {
    private InetAddress address;
    private int peerServerPort;
    private int peerHeartBeatPort;

    public Peer(InetAddress address, int peerServerPort, int peerHeartBeatPort) {
        this.address = address;
        this.peerServerPort = peerServerPort;
        this.peerHeartBeatPort = peerHeartBeatPort;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPeerServerPort() {
        return peerServerPort;
    }

    public int getPeerHeartBeatPort() {
        return peerHeartBeatPort;
    }

    public void writeObject(ObjectOutputStream out) throws IOException{
        out.writeObject(address);
        out.writeInt(peerServerPort);
        out.writeInt(peerHeartBeatPort);
        out.flush();
    }

    public static Peer readObject(ObjectInputStream in) throws IOException,ClassNotFoundException {
        InetAddress adr = (InetAddress)in.readObject();
        int port1 = in.readInt();
        int port2 = in.readInt();

        return new Peer(adr,port1,port2);
    }
}
