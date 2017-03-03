import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;

/**
 * Client class for Server to hold Client related data, which are address, dataPort and heartBeatPort.
 *
 * Created by od on 17.02.2017.
 */
public class Client implements Serializable {
    private InetAddress address;
    private int heartBeatPort;
    private int dataPort;

    public Client(InetAddress address,  int dataPort, int heartBeatPort) {
        this.address = address;
        this.heartBeatPort = heartBeatPort;
        this.dataPort = dataPort;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public int getHeartBeatPort() {
        return heartBeatPort;
    }

    public void setHeartBeatPort(int heartBeatPort) {
        this.heartBeatPort = heartBeatPort;
    }

    public int getDataPort() {
        return dataPort;
    }

    public void setDataPort(int dataPort) {
        this.dataPort = dataPort;
    }

    //Serialization
    public void writeObject(ObjectOutputStream out) throws IOException{
        out.writeObject(address);
        out.writeInt(dataPort);
        out.writeInt(heartBeatPort);
        out.flush();
    }

    //Deserialization
    public Client readObject(ObjectInputStream in) throws IOException,ClassNotFoundException {
        InetAddress adr = (InetAddress)in.readObject();
        int port1 = in.readInt();
        int port2 = in.readInt();

        return new Client(adr,port1,port2);
    }
}
