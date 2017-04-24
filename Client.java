import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client is actual working class for peers.
 *
 * Created by od on 17.02.2017.
 */

public class Client extends Thread {

    private String swAdr;
    private int swPort;
    ConcurrentHashMap<Peer, Integer> peerList;

    private int serverPort;
    private int heartBeatPort;
    private ServerSocket serverSocket;

    /* Each client has its own blockchain */
    private BlockchainManager bcm;

    public Client(String swAdr, int swPort, int heartBeatPort, int serverPort) {
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


            /* Receive the hashcodes of all blocks from one of the active clients */
            Socket peerConnection = null;
            ObjectOutputStream pout;
            ObjectInputStream pin;

            /* Storing the hash codes of blocks */
            ArrayList<String> hashcodes = new ArrayList<String>();

            try {
                /* Search for an active peer to receive the hashcodes */
                for (Peer peer : peerList.keySet()) {
                    try {
                        peerConnection = new Socket(peer.getAddress(), peer.getPeerServerPort());
                        pout = new ObjectOutputStream(new DataOutputStream(peerConnection.getOutputStream()));
                        pout.writeInt(400);
                        pout.flush();

                        System.out.println("New client is connected to a peer\n");
                        continue;

                    } catch (IOException e) {
                        System.out.println("Peer is not found for receiving hashcodes\n");
                    }
                }

                /* Input stream for receiving the hash codes */
                pin = new ObjectInputStream(new DataInputStream(peerConnection.getInputStream()));

                /* Get the hashcodes from active peer and store them in ArrayList */
                try {
                    while (pin.available() > 0)
                    {
                        String s = (String) pin.readObject();
                        hashcodes.add(s);
                    }
                } catch (Exception e) {
                    System.err.println("Hashcode receive interrupted\n.");
                }

                /* Send hashcodes to peers and request blocks from each peer until all the blocks are received */
                while(hashcodes.size() > 0)
                {
                    for (Peer p : peerList.keySet()) {

                            try {
                                Socket blockConnection = new Socket(p.getAddress(), p.getPeerServerPort());
                                ObjectOutputStream bout = new ObjectOutputStream(new DataOutputStream(blockConnection.getOutputStream()));
                                bout.writeInt(500);

                                /* Send the hash code */
                                bout.writeObject(hashcodes.get(0));
                                bout.flush();

                                /* Input Stream for receiving block */
                                ObjectInputStream bin = new ObjectInputStream(new DataInputStream(blockConnection.getInputStream()));

                                /* Receive the block */
                                Block newBlock = Block.readObject(bin);

                                /* Add the block */
                                this.bcm.getBlockchain().addBlock(newBlock);

                                /* Remove hashcode of added block from the ArrayList */
                                hashcodes.remove(0);

                            } catch (Exception e) {
                                System.err.println("Message did not delivered to the peer");
                            }
                    }
                }

            } catch (IOException e) {
                System.out.println("Cannot connected to peer\n");
            }

        } catch (IOException e) {
            System.out.println("Cannot connected to peer\n");
        }

        this.start();
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

    }

    public int getServerPort() {
        return serverPort;
    }

    public int getHeartBeatPort() {
        return heartBeatPort;
    }
}
