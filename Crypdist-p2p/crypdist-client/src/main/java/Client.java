import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client is actual working class for peers.
 *
 * Created by od on 17.02.2017.
 */

public class Client extends Thread{

    private String swAdr;
    private int swPort;
    ConcurrentHashMap<Peer,Integer> peerList;

    private int serverPort;
    private int heartBeatPort;
    private ServerSocket serverSocket;

    /* Each client has its own blockchain */
    private BlockchainManager bcm;

    public Client (String swAdr, int swPort,  int heartBeatPort , int serverPort) {
        this.heartBeatPort = heartBeatPort;
        this.serverPort = serverPort;
        this.swAdr = swAdr;
        this.swPort = swPort;

        /* Initialize the blockchain for new client */
        this.bcm = new BlockchainManager(new Block());

        initialization();
    }

    public BlockchainManager getBCM(){
        return bcm;
    }

    public void initialization() {

        //Establish a connection with server, get number of active peers and their information.
        try {
            Socket serverConnection = new Socket(swAdr, swPort);
            DataInputStream in = new DataInputStream(serverConnection.getInputStream());

            receivePeerList(in);

            //Send itself data to server.
            DataOutputStream out = new DataOutputStream(serverConnection.getOutputStream());
            out.writeInt(serverPort);
            out.writeInt(heartBeatPort);
            out.flush();

            serverConnection.close();


            /* Receive the keymap from one of the clients */
            try
            {
                Socket peerConnection = new Socket(swAdr, swPort);
                ObjectOutputStream pout = new ObjectOutputStream(new DataOutputStream(peerConnection.getOutputStream()));
                pout.writeInt(400);
                pout.flush();

                ObjectInputStream pin = new ObjectInputStream(new DataInputStream(peerConnection.getInputStream()));

                /* Get the keymap from a client */
                try
                {
                    Object o = pin.readObject();

                    /* Ask one block from each client */

                    for(Peer peer : peerList.keySet())
                    {
                        peer.writeObject(new ObjectOutputStream(out));
                    }

                }
                catch(ClassNotFoundException e)
                {
                    System.err.println("Cannot convert to object");
                }

            }
            catch (IOException e){
                System.out.println("Canno connected to peer");
            }

            // TODO receive blockchain from the active peers

                /*int index = 0;

                *//* TODO Full blockchain'i nasil anliyorum *//*
                while( !this.bcm.getBlockchain().equals(bcm.getBlockchain()) )
                {
                    for(Peer peer : peerList.keySet())
                    {
                        if(peer.getAddress().isReachable(10000))
                        {
                            try
                            {
                                Socket peerConnection = new Socket(peer.getAddress(), peer.getPeerServerPort());
                                DataInputStream peerin = new DataInputStream(peerConnection.getInputStream());

                                while(index < bcm.getBlockchain().getLength())
                                {
                                    try
                                    {
                                        Block block = Block.readObject(new ObjectInputStream(peerin));
                                        this.bcm.getBlockchain().addBlock(block);
                                        index++;
                                    }
                                    catch(ClassNotFoundException classException)
                                    {
                                        System.err.println("Block " + index + " cannot be resolved to an object.");
                                    }
                                }
                            }
                            catch(IOException e)
                            {
                                System.err.println("Peer connection is interrupted");
                            }
                        }
                    }
                }
*/

            this.start();
        }
        catch(IOException e)
        {
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
                    //new PeerNotifier(p,heartBeatPort,serverPort).start();
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

    public void broadCastMessage(String message) {
        for(Peer p: peerList.keySet()) {
            sendMessage(p,message);
        }
    }

    public boolean sendMessage(Peer p, String msg) {
        try {
            Socket messagedClient = new Socket(p.getAddress(),p.getPeerServerPort());
            ObjectOutputStream out = new ObjectOutputStream(new DataOutputStream(messagedClient.getOutputStream()));
            out.writeInt(200);
            out.writeObject(msg);
            out.flush();

            messagedClient.close();

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void run() {

        Timer timer = new Timer();
        timer.schedule(new HeartBeatTask(peerList,101), 0, 5 * 1000);

        Thread t1 = new ReceiveHeartBeat(this);
        Thread t2 = new ReceiveServerRequest(this);

        t1.start();
        t2.start();
    /*
        // Receive PeerNotifier request from the new node
        while (this.isAlive())
        {
            new Thread(() -> {
                try {
                    Socket newConnection = serverSocket.accept();
                    ObjectInputStream peerMessage = new ObjectInputStream(newConnection.getInputStream());

                    int m1 = peerMessage.readInt();
                    int m2 = peerMessage.readInt();
                    int m3 = peerMessage.readInt();

                    System.out.println(m1 + "\n" + m2 + "\n" + m3 + "\n");
                    System.out.println("Socket timed out!");
                } catch (IOException e) {
                    System.err.println("Message did not delivered to the peer");
                }
            }).start();
        }*/
    }

    public int getServerPort() {
        return serverPort;
    }

    public int getHeartBeatPort() {
        return heartBeatPort;
    }
}