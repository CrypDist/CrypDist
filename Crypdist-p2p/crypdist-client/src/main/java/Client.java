import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client is actual working class for peers.
 *
 * Created by od on 17.02.2017.
 */

public class Client {

    private String swAdr;
    private int swPort;
    ConcurrentHashMap<Peer,Integer> peerList;

    private int serverPort;
    private int heartBeatPort;


    public Client (String swAdr, int swPort,  int heartBeatPort , int serverPort) {
        this.heartBeatPort = heartBeatPort;
        this.serverPort = serverPort;
        this.swAdr = swAdr;
        this.swPort = swPort;

        initialization();
    }

    public void initialization(){

        //Establish a connection with server, get number of active peers and their information.
        try {
            Socket serverConnection = new Socket(swAdr,swPort);
            DataInputStream in = new DataInputStream(serverConnection.getInputStream());

            receivePeerList(in);

            //Send itself data to server.
            DataOutputStream out = new DataOutputStream(serverConnection.getOutputStream());
            out.writeInt(serverPort);
            out.writeInt(heartBeatPort);
            out.flush();

            serverConnection.close();

            run();
        }
        catch (IOException e) {
            System.err.println("Cannot connect to the server, terminated.");
        }
    }

    public void receivePeerList(DataInputStream in) throws IOException{

        try {
            int peerSize = in.readInt();
            peerList = new ConcurrentHashMap<>(peerSize);

            for(int i = 0; i < peerSize ; i++) {
                try {
                    Peer p = Peer.readObject(new ObjectInputStream(in));
                    peerList.put(p,0);
                    new PeerNotifier(p,heartBeatPort,serverPort).start();
                }
                catch (ClassNotFoundException classException) {
                    System.err.println("Peer " + i + " cannot be resolved to an object.");
                }
            }
        } catch (IOException e) {
            System.err.println("Cannot read from the server socket.");
            throw e;
        }

        System.out.println("Client initialized with size: " + peerList.size());

    }
    public void run() {

        Timer timer = new Timer();
        timer.schedule(new HeartBeatTask(peerList), 0, 5 * 1000);

        Thread t1 = new ReceiveHeartBeat(this);
        Thread t2 = new ReceiveServerRequest(this);

        t1.start();
        t2.start();
    }

    public int getServerPort() {
        return serverPort;
    }

    public int getHeartBeatPort() {
        return heartBeatPort;
    }
}
