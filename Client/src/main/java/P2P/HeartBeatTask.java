package P2P;

import org.apache.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * P2P.HeartBeatTask class for both Server and P2P.Client for refreshing their peerList by sending heartbeats.
 *
 * Created by od on 17.02.2017.
 */


public class HeartBeatTask extends TimerTask {

    private static Logger log = Client.log;

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

                out.writeInt(101);  //0 for heartbeats
                out.writeInt(hbPort);
                out.writeInt(swPort);

                out.flush();
                clientSocket.setSoTimeout(10000);

                int x = in.readInt();
                if(x == 102) {
                    return peer;
                }
            } catch (IOException e) {
                return null;
            }
            return null;
        }
    }


    private ConcurrentHashMap<Peer,Integer> peerList;
    private static Client client;
    private int hbPort;
    private int swPort;
    private int size;

    public HeartBeatTask(Client client, ConcurrentHashMap<Peer,Integer> peerList, int flag) {
        this.peerList = peerList;
        HeartBeatTask.client = client;
        this.size =peerList.size();
        if (size != 0) {
            client.change();
            client.notifyObservers("X////" + size);
        }
    }


    public HeartBeatTask(Client client, ConcurrentHashMap<Peer,Integer> peerList, int hbPort, int swPort) {
        this.peerList = peerList;
        this.hbPort = hbPort;
        this.swPort = swPort;
        this.size =peerList.size();
        HeartBeatTask.client = client;
        if(HeartBeatTask.client == null)
        {
            System.out.println("Helloooo!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
        if (size != 0) {
            HeartBeatTask.client.change();
            HeartBeatTask.client.notifyObservers("X////" + size);
        }
    }


    public void run() {
        for(Map.Entry<Peer,Integer> entry : peerList.entrySet() ) {
            if (entry.getValue() > 3) {
                peerList.remove(entry.getKey());
            }
            else {
                peerList.put(entry.getKey(), entry.getValue() + 1);
            }
        }

        ExecutorService executor = Executors.newCachedThreadPool();
        ArrayList<Future<Peer>> results = new ArrayList<>();
        for(Peer peer:peerList.keySet()) {
            Callable<Peer> task = new SendHeartBeat(peer);
            Future<Peer> future = executor.submit(task);
            results.add(future);
        }

        try {
            for(Future<Peer> future: results) {
                Peer p = future.get();
                if(p != null)
                    peerList.put(p,0);
            }

        } catch (Exception e) {

        }
        int a = peerList.size();

        if (size != a) {
            HeartBeatTask.client.change();
            HeartBeatTask.client.notifyObservers("X////" + a);
            size = a;
        }
    }


}