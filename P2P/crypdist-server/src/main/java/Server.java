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

    HashSet<Client> peerList;
    private ServerSocket serverSocket;

    public Server(int port) throws IOException {
        peerList = new HashSet<Client>();

        //Opening serverSocket
        serverSocket = new ServerSocket(port);

    }

    public void run() {

        //Timer action is used for periodical heartbeats to clients.
        Timer timer = new Timer();
        timer.schedule(new runWithTime(), 100000, 100 * 1000);

        //Server constantly accepts for new client connections.
        //Recall that serverSocket.accept() is a blocking call.
        while (true) {
            try {
                Socket newConnection = serverSocket.accept();
                DataOutputStream out = new DataOutputStream(newConnection.getOutputStream());

                //Whenever a new client is connected, the number of alive clients and their objects are sent.
                out.writeInt(peerList.size());

                for(Client client : peerList) {
                    client.writeObject(new ObjectOutputStream(out));
                }

                //The data for dataPort and heartBeatPort for newly connected client is taken from same socket.
                DataInputStream in = new DataInputStream(newConnection.getInputStream());
                int port = in.readInt();
                int port2 = in.readInt();

                System.out.println(newConnection.getInetAddress() + " is connected.");
                peerList.add( new Client(newConnection.getInetAddress(),port,port2 ));

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

    /*
     * Each heartbeat is sent in a separate thread.
     * Each task has a client property to send the heartbeat.
     * If heartbeat's response is taken, then the task's client is returned.
     */
    public class runWithTime extends TimerTask {
        private class SendHeartBeat implements Callable<Client> {
            private Client client;

            public SendHeartBeat(Client client) {
                this.client = client;
            }

            public Client call() {
                try {
                    //Open a new socket to client's heartbeat port.
                    Socket clientSocket = new Socket(client.getAddress(),client.getHeartBeatPort());

                    //Send 0 as heartbeat flag, the response of this should be 1.
                    DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                    DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                    out.writeInt(0);  //0 for heartbeats
                    out.flush();

                    //-1 is represent the end of stream in a socket.
                    // It reads until a proper integer is read from socket, or this call will timeout.
                    int x = in.readInt();
                    while(x == -1) {
                        x = in.readInt();
                    }
                    if(x == 1) {
                        return client;
                    }
                } catch (IOException e) {
                    System.out.println("Client disconnected.");
                }
                return null;
            }
        }
        public void run() {

            //For each client, a task is created.
            ExecutorService executor = Executors.newCachedThreadPool();
            ArrayList<Future<Client>> results = new ArrayList<>();
            for(Client client:peerList) {
                Callable<Client> task = new SendHeartBeat(client);
                Future<Client> future = executor.submit(task);
                results.add(future);
            }

            //Wait 5 seconds for heartbeat responses and try to get results without waiting(1ms)
            ArrayList<Client> clients = new ArrayList<>();
            try {
                Thread.sleep(5000);

                for(Future<Client> future: results) {
                    Client c = future.get(1,TimeUnit.MILLISECONDS);
                    if(c != null)
                        clients.add(c);
                }

            } catch (Exception e) {
                System.out.println("Interrupted.");
            }

            //Debug print.
            System.out.println(peerList.size()-clients.size() + " is disconnected.");
            peerList.retainAll(clients);
        }
    }
}
