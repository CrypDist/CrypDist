
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by od on 17.02.2017.
 */
public class Client extends Thread {

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

        System.out.println("HB is opening from " + heartBeatPort);
        heartBeatSocket = new ServerSocket(heartBeatPort);
        Socket serverConnection = new Socket(swAdr,swPort);
        DataInputStream in = new DataInputStream(serverConnection.getInputStream());
        int peerSize = in.readInt();
        peerList = new HashSet<>(peerSize);

        for(int i = 0; i < peerSize ; i++) {
            Peer p =Peer.readObject(new ObjectInputStream(in));
            peerList.add(p);
        }

        DataOutputStream out = new DataOutputStream(serverConnection.getOutputStream());
        out.writeInt(serverPort);
        out.writeInt(heartBeatPort);
        out.flush();

        serverConnection.close();
    }

    public void run() {

        Timer timer = new Timer();
        timer.schedule(new runWithTime(), 0, 10 * 1000);

        while (true) {
            try {
                Socket hb = heartBeatSocket.accept();
                System.out.println("Hb recieved.");
                new Thread(() -> {
                    try {
                        DataInputStream in = new DataInputStream(hb.getInputStream());
                        int flag = in.readInt();
                        System.out.println("Flag: " + flag);

                        if(flag == 0) {
                            DataOutputStream out = new DataOutputStream(hb.getOutputStream());
                            out.writeInt(1);
                            out.flush();
                        }

                        hb.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
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
        private class SendHeartBeat implements Callable<Peer> {
            private Peer peer;
            public SendHeartBeat(Peer peer) {
                this.peer = peer;
            }
            public Peer call() {
                try {
                    Socket clientSocket = new Socket(peer.getAddress(),peer.getPeerHeartBeatPort());
                    DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                    DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                    out.writeInt(0);  //0 for heartbeats
                    out.flush();
                    int x = in.readInt();
                    while(x == -1) {
                        x = in.readInt();
                    }
                    if(x == 1) {
                        return peer;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }
        public void run() {

            System.out.println("Size: " + peerList.size());
            ExecutorService executor = Executors.newCachedThreadPool();
            ArrayList<Future<Peer>> results = new ArrayList<>();
            for(Peer peer:peerList) {
                Callable<Peer> task = new SendHeartBeat(peer);
                Future<Peer> future = executor.submit(task);
                results.add(future);
            }

            ArrayList<Peer> peers = new ArrayList<>();
            try {
                Thread.sleep(5000);

                for(Future<Peer> future: results) {
                    Peer p = future.get(1,TimeUnit.MILLISECONDS);
                    if(p != null)
                        peers.add(p);
                }

            } catch (Exception e) {
                System.out.println("Interrupted.");
            }

            System.out.println(peerList.size()-peers.size() + " is disconnected.");
            peerList.retainAll(peers);
        }
    }
}
