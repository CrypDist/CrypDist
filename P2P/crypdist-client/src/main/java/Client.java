import javax.xml.crypto.Data;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

/**
 * Client is actual working class for peers.
 *
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

    private class HeartBeatTask implements Runnable {

        private ServerSocket heartBeatSocket;

        public HeartBeatTask(ServerSocket heartBeatSocket) {
            this.heartBeatSocket = heartBeatSocket;
        }
        public void run() {
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
    }

    private class ServerTask implements Runnable {

        private ServerSocket serverSocket;

        public ServerTask(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;
        }

        public void run() {
            System.out.println("Accepting server..");
            while (true) {
                try {
                    Socket incomingRequest = serverSocket.accept();

                    System.out.println("Accepted:" + incomingRequest.getInetAddress().getCanonicalHostName());
                    new Thread(() -> {
                        try {
                            DataInputStream in = new DataInputStream(incomingRequest.getInputStream());
                            int flag = in.readInt();

                            System.out.println("Fflag:" + flag);

                            DataOutputStream out = new DataOutputStream(incomingRequest.getOutputStream());
                            switch (flag){
                                case 1:
                                    out.writeInt(0);
                                    break;
                                case 2:
                                    Peer p = Peer.readObject(new ObjectInputStream(in));
                                    out.writeInt(1);
                                    peerList.add(p);
                                    break;
                                case 3:
                                    break;
                                case 4:
                                    break;
                            }

                            out.flush();

                            incomingRequest.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
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
    }
    public void run() {

        Timer timer = new Timer();
        timer.schedule(new runWithTime(), 0, 10 * 1000);

        Thread t1 = new Thread(new HeartBeatTask(heartBeatSocket));
        Thread t2 = new Thread(new ServerTask(serverSocket));

        t1.start();
        t2.start();
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
