import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server implementation for establishing connection among peers.
 *
 * The list of clients are held in server to be sent whenever a new client is connected.
 *
 * Periodically, server sends heartbeats to clients to check whether they are alive or not. The clients with no response
 *  are deleted from list.
 *
 * Created by od on 16.02.2017.
 */
public class Server extends Thread {

    ConcurrentHashMap<Peer,Integer> peerList;
    private ServerSocket serverSocket;

    public Server(int port) throws IOException {
        peerList = new ConcurrentHashMap<>();

        //Opening serverSocket
        serverSocket = new ServerSocket(port);

    }

    public void run() {

        //Timer action is used for periodical heartbeats to clients.
        Timer timer = new Timer();
        timer.schedule(new HeartBeatTask(peerList, 100), 1000, 5 * 1000);

        //Server constantly accepts for new client connections.
        //Recall that serverSocket.accept() is a blocking call.
        while (true) {
            try {
                Socket newConnection = serverSocket.accept();
                DataOutputStream out = new DataOutputStream(newConnection.getOutputStream());

                //Whenever a new client is connected, the number of alive clients and their objects are sent.
                out.writeInt(peerList.size());

                for(Peer peer : peerList.keySet()) {
                    peer.writeObject(new ObjectOutputStream(out));
                }

                //The data for dataPort and heartBeatPort for newly connected client is taken from same socket.
                DataInputStream in = new DataInputStream(newConnection.getInputStream());
                int port = in.readInt();
                int port2 = in.readInt();

                System.out.println(newConnection.getInetAddress() + " is connected.");
                peerList.put( new Peer(newConnection.getInetAddress(),port,port2 ),0);

                newConnection.close();
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
