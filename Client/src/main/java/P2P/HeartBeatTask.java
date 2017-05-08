package P2P;

import Util.Config;
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
            Socket clientSocket = null;
            try {
                clientSocket = new Socket(peer.getAddress(),peer.getPeerHeartBeatPort());
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());

                out.writeInt(Config.HEARTBEAT_FLAG_CLIENT);  //0 for heartbeats
                out.writeInt(hbPort);
                out.writeInt(swPort);

                out.flush();
                clientSocket.setSoTimeout(Config.HEARTBEAT_TIMEOUT);

                int x = in.readInt();
                clientSocket.close();
                if(x == Config.HEARTBEAT_ACK) {
                    return peer;
                }
            } catch (IOException e) {
                return null;
            } finally {
                if(clientSocket != null && !clientSocket.isClosed())
                    try {
                        clientSocket.close();
                    } catch (Exception e) {

                    }
            }
            return null;
        }
    }


    private ConcurrentHashMap<Peer,Integer> peerList;
    private static Client client;
    private int hbPort;
    private int swPort;
    private int size;


    public HeartBeatTask(Client client, ConcurrentHashMap<Peer,Integer> peerList, int hbPort, int swPort) {
        this.peerList = peerList;
        this.hbPort = hbPort;
        this.swPort = swPort;
        this.size =peerList.size();
        HeartBeatTask.client = client;
    }


    public void run() {
        for(Map.Entry<Peer,Integer> entry : peerList.entrySet() ) {
            if (entry.getValue() > Config.HEARTBEAT_MAX_TRIALS) {
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
            client.notify(Config.CLIENT_MESSAGE_PEERSIZE + Config.CLIENT_MESSAGE_SPLITTER + a);
            size = a;
        }
    }


}