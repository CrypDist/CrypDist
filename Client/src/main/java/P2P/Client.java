package P2P;

import Util.CrypDist;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client is actual working class for peers.
 *
 * Created by od on 17.02.2017.
 */

public class Client extends Observable implements Runnable{

    static transient  Logger log = Logger.getLogger("P2P");
    private CrypDist crypDist;

    private String swAdr;
    private int swPort;
    ConcurrentHashMap<Peer,Integer> peerList;
    private int serverPort;
    private int heartBeatPort;

    // Added to support the hash choosing algorithm.
    int lastSize;


    public Client (String swAdr, int swPort,  int heartBeatPort , int serverPort,
                   CrypDist crypDist) {
        this.crypDist = crypDist;
        this.heartBeatPort = heartBeatPort;
        this.serverPort = serverPort;
        this.swAdr = swAdr;
        this.swPort = swPort;
        lastSize = 0;

        initialization();
    }

    public void change(){
        setChanged();
    }
    public void initialization() {

        //Establish a connection with server, get number of active peers and their information.
        try {
            Socket serverConnection = new Socket(swAdr, swPort);
            serverConnection.setSoTimeout(3000);

            DataInputStream in = new DataInputStream(serverConnection.getInputStream());

            receivePeerList(in);

            //Send itself data to server.
            DataOutputStream out = new DataOutputStream(serverConnection.getOutputStream());
            out.writeInt(heartBeatPort);
            out.writeInt(serverPort);
            out.flush();

            serverConnection.close();
        }
        catch(IOException e)
        {
            log.fatal("Cannot connect to the server, terminated.");
            log.trace(e);
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
                    //new Peer8Notifier(p,heartBeatPort,serverPort).start();
                }
                catch (ClassNotFoundException classException) {
                    log.error("A peer cannot be resolved to an object.");
                    log.trace(classException);
                }
            }
        } catch (IOException e) {
            throw e;
        }

        log.info("Client initialized with size: " + peerList.size());

    }

    public void broadCastMessage(String message) {
        for(Peer p: peerList.keySet()) {
            sendMessage(p,message,0);
        }
    }

    public boolean sendMessage(String adr, String msg) {
        for(Peer p: peerList.keySet()){
            if(p.getAddress().toString().equals(adr)){
                return sendMessage(p,msg,0);
            }
        }
        return false;
    }

    public boolean sendMessage(Peer p, String msg,int trials) {

        if (trials > 4) {
            log.error("Message cannot be sent after 5 trials");
            log.trace(msg);
        }
        try {
            log.trace(p.getPeerServerPort());
            log.trace(p.getAddress());
            Socket messagedClient = new Socket(p.getAddress(),p.getPeerServerPort());
            ObjectOutputStream out = new ObjectOutputStream(messagedClient.getOutputStream());
            out.writeInt(200);
            out.writeUTF(msg);
            out.flush();

            ObjectInputStream in = new ObjectInputStream(new DataInputStream(messagedClient.getInputStream()));
            int ack = in.readInt();
            messagedClient.close();
            log.info("Message is sent!");

            if(ack != 900) {
                log.trace("Non flag read");
                return sendMessage(p,msg,trials+1);
            }


            return true;
        } catch (IOException e) {
            return sendMessage(p,msg,trials+1);
        }
    }

    public void run() {

        Timer timer = new Timer();
        timer.schedule(new HeartBeatTask(this, peerList, heartBeatPort,serverPort), 0, 5 * 1000);

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

    public HashSet<String> receiveKeySet()
    {
        return null;
    }

    public HashMap<String, JsonObject> receiveBlocks(Set<String> neededBlocks)
    {
        return null;
    }
}
