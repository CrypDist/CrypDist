import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;

/**
 * Peer class for Peers to hold peer related data, which are address, serverPort(dataPort) and heartBeatPort.
 * It is the corresponding class of Client class on the server side.
 *
 * Created by od on 16.02.2017.
 */

public class Peer implements Serializable {
    private InetAddress address;
    private int peerServerPort;
    private int peerHeartBeatPort;

    public Peer(InetAddress address, int peerHeartBeatPort,int peerServerPort) {
        this.address = address;
        this.peerServerPort = peerServerPort;
        this.peerHeartBeatPort = peerHeartBeatPort;
    }

    public Peer(Peer p) {
        address = p.getAddress();
        peerServerPort = p.getPeerServerPort();
        peerHeartBeatPort = p.getPeerHeartBeatPort();
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

    //Serialization
    public void writeObject(ObjectOutputStream out) throws IOException{
        out.writeObject(this);
    }

    //Deserialization
    public static Peer readObject(ObjectInputStream in) throws IOException,ClassNotFoundException {
        return (Peer)in.readObject();
    }
}
