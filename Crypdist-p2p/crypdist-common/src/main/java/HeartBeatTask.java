import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * HeartBeatTask class for both Server and Client for refreshing their peerList by sending heartbeats.
 *
 * Created by od on 17.02.2017.
 */

public class HeartBeatTask extends TimerTask {

    private ConcurrentHashMap<Peer,Integer> peerList;

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
                return null;
            }
            return null;
        }
    }

    public HeartBeatTask(ConcurrentHashMap<Peer,Integer> peerList) {
        this.peerList = peerList;
    }
    public void run() {


        int a = peerList.size();

        ConcurrentHashMap<Peer,Integer> checkedList = new ConcurrentHashMap<>(peerList);

        ExecutorService executor = Executors.newCachedThreadPool();
        ArrayList<Future<Peer>> results = new ArrayList<>();
        for(Peer peer:checkedList.keySet()) {
            Callable<Peer> task = new SendHeartBeat(peer);
            Future<Peer> future = executor.submit(task);
            results.add(future);
        }

        ArrayList<Peer> peers = new ArrayList<>();
        try {
            Thread.sleep(5000);

            for(Future<Peer> future: results) {
                Peer p = future.get(1000, TimeUnit.MILLISECONDS);
                if(p != null)
                    peers.add(p);
            }

        } catch (Exception e) {
            System.out.println("Interrupted.");
            e.printStackTrace();
        }

        System.out.println("Sent to: " + a + " and " + (checkedList.size()-peers.size()) + " is disconnected.");
        checkedList.keySet().removeAll(peers);
        peerList.keySet().removeAll(checkedList.keySet());

    }


}