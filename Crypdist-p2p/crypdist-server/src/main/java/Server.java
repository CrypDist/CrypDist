import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.*;

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

    HashSet<Peer> peerList;
    private ServerSocket serverSocket;

    public Server(int port) throws IOException {
        peerList = new HashSet<Peer>();

        //Opening serverSocket
        serverSocket = new ServerSocket(port);

    }

    public void run() {

        //Timer action is used for periodical heartbeats to clients.
        Timer timer = new Timer();
        timer.schedule(new HeartBeatTask(peerList), 100000, 100 * 1000);

        //Server constantly accepts for new client connections.
        //Recall that serverSocket.accept() is a blocking call.
        while (true) {
            try {
                Socket newConnection = serverSocket.accept();
                DataOutputStream out = new DataOutputStream(newConnection.getOutputStream());

                //Whenever a new client is connected, the number of alive clients and their objects are sent.
                out.writeInt(peerList.size());

                for(Peer peer : peerList) {
                    peer.writeObject(new ObjectOutputStream(out));
                }

                //The data for dataPort and heartBeatPort for newly connected client is taken from same socket.
                DataInputStream in = new DataInputStream(newConnection.getInputStream());
                int port = in.readInt();
                int port2 = in.readInt();

                System.out.println(newConnection.getInetAddress() + " is connected.");
                peerList.add( new Peer(newConnection.getInetAddress(),port,port2 ));

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
