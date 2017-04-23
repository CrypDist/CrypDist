package P2P;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;

/**
 * P2P.Peer class for Peers to hold peer related data, which are address, serverPort(dataPort) and heartBeatPort.
 * It is the corresponding class of P2P.Client class on the server side.
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


    @Override
    public int hashCode() {
        return address.getHostAddress().hashCode();
    }


    @Override
    public boolean equals(Object obj) {

        if (obj instanceof  Peer) {
            Peer p = (Peer)obj;
            if(address.getHostAddress().equals(p.getAddress().getHostAddress()))
                return true;
        }
        return false;
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
        int hb = in.readInt();
        int sw = in.readInt();
        InetAddress adr = (InetAddress) in.readObject();
        return new Peer(adr,hb,sw);
    }
}
