import javax.xml.crypto.Data;
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
 * Created by od on 16.02.2017.
 */
public class Server extends Thread {

    HashSet<Client> peerList;
    private ServerSocket serverSocket;
    private ServerSocket heartBeatSocket;
    private int heartBeatPort;

    public Server(int port , int port2) throws IOException {
        System.out.println("Server is opening from " + port);
        serverSocket = new ServerSocket(port);
        peerList = new HashSet<Client>();
        heartBeatPort = port2;
        heartBeatSocket = new ServerSocket(heartBeatPort);
        heartBeatSocket.setReuseAddress(true);
    }

    private void sendPeerList(Socket socket) throws IOException {
        //Serialize and send the socket.
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.writeInt(0);
    }


    public void run() {

        Timer timer = new Timer();
        timer.schedule(new runWithTime(), 0, 10 * 1000);

        while (true) {
            try {
                Socket server = serverSocket.accept();
                DataOutputStream out = new DataOutputStream(server.getOutputStream());
                out.writeInt(peerList.size());

                for(Client client : peerList) {
                    client.writeObject(new ObjectOutputStream(out));
                }

                DataInputStream in = new DataInputStream(server.getInputStream());
                int port = in.readInt();
                int port2 = in.readInt();

                System.out.println(server.getInetAddress() + " is connected.");
                peerList.add( new Client(server.getInetAddress(),port,port2 ));
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

    public class runWithTime extends TimerTask {
        private class SendHeartBeat implements Callable<Client> {
            private Client client;
            public SendHeartBeat(Client client) {
                this.client = client;
            }
            public Client call() {
                try {
                    Socket clientSocket = new Socket(client.getAddress(),client.getHeartBeatPort());

                    DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                    DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                    out.writeInt(0);  //0 for heartbeats
                    out.flush();

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

            ExecutorService executor = Executors.newCachedThreadPool();
            ArrayList<Future<Client>> results = new ArrayList<>();
            for(Client client:peerList) {
                Callable<Client> task = new SendHeartBeat(client);
                Future<Client> future = executor.submit(task);
                results.add(future);
            }

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

            System.out.println(peerList.size()-clients.size() + " is disconnected.");
            peerList.retainAll(clients);
        }
    }
}
