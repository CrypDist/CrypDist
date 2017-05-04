package P2P;

import Util.CrypDist;
import Util.Config;
import com.google.gson.Gson;

import com.google.gson.JsonObject;
import com.mchange.v2.collection.MapEntry;
import org.apache.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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


    public Client (CrypDist crypDist) {

        this.crypDist = crypDist;
        heartBeatPort = Config.CLIENT_HEARTBEAT_PORT;
        serverPort = Config.CLIENT_SERVER_PORT;
        swAdr = Config.SERVER_ADDRESS;
        swPort = Config.SERVER_PORT;
        lastSize = 0;

        initialization();
    }

    public String notify(String msg){
        return crypDist.updateByClient(msg);
    }
    public void initialization() {

        System.out.println("x");
        //Establish a connection with server, get number of active peers and their information.
        try {
            Socket serverConnection = new Socket(swAdr, swPort);
            serverConnection.getReuseAddress(true);
            serverConnection.setSoTimeout(Config.SERVER_TIMEOUT);

            DataInputStream in = new DataInputStream(serverConnection.getInputStream());

            receivePeerList(in);

            //Send itself data to server.
            DataOutputStream out = new DataOutputStream(serverConnection.getOutputStream());
            out.writeInt(heartBeatPort);
            out.writeInt(serverPort);
            out.writeUTF(Config.USER_NAME);
            out.writeUTF(Config.USER_PASS);
            out.flush();

            boolean authenticated = in.readBoolean();
            int size = in.readInt();
            byte[] key_array = new byte[size];
            in.read(key_array);

            crypDist.setSessionKey(key_array);
            crypDist.setAuthenticated(authenticated);

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

        ArrayList<Thread> threads = new ArrayList<>(peerList.size());
        for(Peer peer: peerList.keySet()) {
            Thread t = new Thread(new MessageTask(peer,message));
            threads.add(t);
            t.start();
        }

        try {
            for(Thread t: threads) {
                t.join();
            }
        } catch (InterruptedException e) {
            log.trace("Message interrupted.");
        }

    }

    public void sendMessage(String adr, String message) {

        Thread t = null;
        for(Peer peer: peerList.keySet()){
            if(peer.getAddress().toString().equals(adr)){
                 t = new Thread(new MessageTask(peer,message));
                 t.start();
            }
        }
        if(t == null){
            log.error("Peer cannot found.");
        } else {
            try {
                t.join();
            } catch (InterruptedException e) {
                log.error("Message sending is interrupted before success.");
            }
        }
    }

    public ArrayList<String> broadCastMessageResponse(String message) {

        ExecutorService executor = Executors.newCachedThreadPool();
        ArrayList<Future<String>> futures = new ArrayList<>();

        log.info("BROADCASTED TO " + peerList.size() + " PEERS");
        for(Peer peer:peerList.keySet()) {
            Callable<String> task = new ResponsedMessageTask(peer,message);
            Future<String> future = executor.submit(task);
            futures.add(future);
        }

        ArrayList<String> result = new ArrayList<>();

        try {
            for(Future<String> future: futures) {
                String res = future.get();
                if(res != null && !res.equals(""))
                {
                    log.info("RESULT IS ADDED: " + res);
                    result.add(res);
                }
                else
                    log.error("KEYSET CANNOT BE RECEIVED.");
            }
        } catch (Exception e) {
            log.error("KEYSET CANNOT BE RECEIVED.");
        }

        return result;
    }

    public HashMap<String,String> broadCastMessageResponseWithIp(String message) {

        ExecutorService executor = Executors.newCachedThreadPool();
        HashMap<Peer,Future<String>> futures = new HashMap<>();

        for(Peer peer:peerList.keySet()) {
            Callable<String> task = new ResponsedMessageTask(peer,message);
            Future<String> future = executor.submit(task);
            futures.put(peer,future);
        }

        HashMap<String,String> result = new HashMap<>();


        for(Map.Entry<Peer,Future<String>> entry : futures.entrySet()) {
            try {
                String res  = entry.getValue().get();
                if(res != null && !res.equals(""))
                    result.put(entry.getKey().getAddress().toString(),res);
            } catch (Exception e) {

            }
        }

        return result;
    }



    public String sendMessageResponse(String adr, String message) {

        Future<String> f = null;
        ExecutorService executor = Executors.newCachedThreadPool();
        for(Peer peer: peerList.keySet()){
            if(peer.getAddress().toString().equals(adr)){
                Callable<String> task = new ResponsedMessageTask(peer,message);
                f = executor.submit(task);
            }
        }

        if(f != null) {
            try {
                return f.get();
            } catch (Exception e) {
                return null;
            }
        }
        return null; // OR "" can be returned.s
    }


    public void run() {
        //To notify at the beginning
        notify(Config.CLIENT_MESSAGE_PEERSIZE + Config.CLIENT_MESSAGE_SPLITTER + peerList.size());

        Timer timer = new Timer();
        timer.schedule(new HeartBeatTask(this, peerList, heartBeatPort,serverPort), 0, Config.HEARTBEAT_PERIOD);

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

    public HashMap<String, String > receiveBlocks(Set<String> neededBlocks) {

        log.info("RECEIVE BLOCKS ARE CALLED");

        for(String s : neededBlocks)
            log.info("STRING:" + s);

        int peerSize = peerList.size();
        Peer[] peers = new Peer[peerSize];
        Iterator<Peer> i = peerList.keySet().iterator();
        int count = 0;
        while(i.hasNext()){
            peers[count++] = i.next();
        }

        HashSet<String> remainingTasks = new HashSet<>(neededBlocks);
        HashMap<String,String> actualResults = new HashMap<>();

        Random r = new Random();
        while(remainingTasks.size() > 0) {
            count = r.nextInt(peerSize);

            Iterator<String> iterator = remainingTasks.iterator();
            HashMap<String, Future<String>> assignments = new HashMap<>();
            ExecutorService executor = Executors.newCachedThreadPool();
            while(iterator.hasNext()){
                String hash = iterator.next();

                //Message to be sent for requesting block
                JsonObject obj = new JsonObject();
                obj.addProperty("flag",Config.MESSAGE_REQUEST_BLOCK);
                obj.addProperty("data", hash);

                Gson gson = new Gson();
                Callable<String> task = new ResponsedMessageTask(peers[count % peerSize], gson.toJson(obj, JsonObject.class));
                Future<String> f = executor.submit(task);

                assignments.put(hash,f);
                log.info("AN ASSIGNMENT IS DONE.");

                count++;
            }

            for(Map.Entry<String, Future<String>> entry : assignments.entrySet()) {
                try {
                    String response = entry.getValue().get();
                    if(response != null) {
                        actualResults.put(entry.getKey(),response);
                        remainingTasks.remove(entry.getKey());
                    }
                    log.info("A RESULT IS TAKEN");
                } catch (Exception e) {
                    log.fatal("RESPONSE CANNOT BE TAKEN.");
                }
            }
        }


        return actualResults;
    }

    public ArrayList<String> receiveKeySet() {
        log.info("KEY SET IS CALLED WITH " + peerList.size() + " PEERS");
        JsonObject obj = new JsonObject();
        obj.addProperty("flag",Config.MESSAGE_REQUEST_KEYSET);
        return broadCastMessageResponse(obj.toString());
    }
}
