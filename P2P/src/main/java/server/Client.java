package server;

import java.net.InetAddress;

/**
 * Created by od on 17.02.2017.
 */
public class Client {
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
}
