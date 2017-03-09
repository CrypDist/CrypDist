import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TimerTask;
import java.util.concurrent.*;

/**
 * HeartBeatTask class for both Server and Client for refreshing their peerList by sending heartbeats.
 *
 * Created by od on 17.02.2017.
 */

public class HeartBeatTask extends TimerTask {

    private HashSet<Peer> peerList;

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

    public HeartBeatTask(HashSet<Peer> peerList) {
        this.peerList = peerList;
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
                Peer p = future.get(1, TimeUnit.MILLISECONDS);
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