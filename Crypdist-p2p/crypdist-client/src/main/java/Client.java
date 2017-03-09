import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Timer;
/**
 * Client is actual working class for peers.
 *
 * Created by od on 17.02.2017.
 */

public class Client extends Thread {

    private BlockchainManager blockchainManager;
    private String swAdr;
    private int swPort;
    HashSet<Peer> peerList;

    private int serverPort;
    private int heartBeatPort;
    private ServerSocket serverSocket;
    private ServerSocket heartBeatSocket;


    public Client (String swAdr, int swPort, int serverPort, int heartBeatPort) throws IOException,ClassNotFoundException {
        this.heartBeatPort = heartBeatPort;
        this.serverPort = serverPort;
        this.swAdr = swAdr;
        this.swPort = swPort;

        initialization();
    }

    public void initialization() throws IOException,ClassNotFoundException {

        //Establish a connection with server, get number of active peers and their information.
        Socket serverConnection = new Socket(swAdr,swPort);
        DataInputStream in = new DataInputStream(serverConnection.getInputStream());
        int peerSize = in.readInt();
        peerList = new HashSet<>(peerSize);

        for(int i = 0; i < peerSize ; i++) {
            Peer p =Peer.readObject(new ObjectInputStream(in));

            new Thread(() -> {
                try {
                    Socket clientSocket = new Socket(p.getAddress(),p.getPeerServerPort());
                    DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                    out.writeInt(2);  //2 for new cons
                    new Peer(InetAddress.getLoopbackAddress(),serverPort,heartBeatPort).writeObject(new ObjectOutputStream(out));
                    out.flush();

                    DataInputStream in2 = new DataInputStream(clientSocket.getInputStream());
                    int result = in2.readInt();

                    if(result != 1)
                        System.out.println("Error that should be handled");

                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
            peerList.add(p);
        }

        //Send itself data to server.
        DataOutputStream out = new DataOutputStream(serverConnection.getOutputStream());
        out.writeInt(serverPort);
        out.writeInt(heartBeatPort);
        out.flush();

        serverConnection.close();

        //Opening heartbeat socket for accepting heartbeat connections..
        heartBeatSocket = new ServerSocket(heartBeatPort);

        //Opening server port for accepting data connections.
        serverSocket = new ServerSocket(serverPort);

    }

    public void run() {

        Timer timer = new Timer();
        timer.schedule(new HeartBeatTask(peerList), 0, 10 * 1000);

        Thread t1 = new Thread(new ReceiveHeartBeat(heartBeatSocket));
        Thread t2 = new Thread(new ReceiveServerRequest(serverSocket,this));

        t1.start();
        t2.start();
    }

}
