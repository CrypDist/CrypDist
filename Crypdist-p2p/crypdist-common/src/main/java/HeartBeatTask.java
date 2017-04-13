import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
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

    private class SendHeartBeat implements Callable<Peer> {
        private Peer peer;
        private int flag;
        private int hbPort;
        private int swPort;

        public SendHeartBeat(Peer peer, int flag) {
            this.peer = peer;
            this.flag = flag;
        }

        public SendHeartBeat(Peer peer, int flag,int hbPort, int swPort) {
            this.peer = peer;
            this.flag = flag;
            this.hbPort = hbPort;
            this.swPort = swPort;
        }

        public Peer call() {
            try {
                Socket clientSocket = new Socket(peer.getAddress(),peer.getPeerHeartBeatPort());
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());

                out.writeInt(flag);  //0 for heartbeats
                if(flag == 101) {
                    out.writeInt(hbPort);
                    out.writeInt(swPort);
                }
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
    private int flag;
    private int hbPort;
    private int swPort;

    public HeartBeatTask(ConcurrentHashMap<Peer,Integer> peerList, int flag) {
        this.peerList = peerList;
        this.flag = flag;
    }

    public HeartBeatTask(ConcurrentHashMap<Peer,Integer> peerList, int flag, int hbPort, int swPort) {
        this.peerList = peerList;
        this.flag = flag;
        this.hbPort = hbPort;
        this.swPort = swPort;
    }

    public void run() {


        ConcurrentHashMap<Peer,Integer> checkedList = new ConcurrentHashMap<>(peerList);

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
        for(Peer peer:checkedList.keySet()) {
            Callable<Peer> task = new SendHeartBeat(peer,flag);
            Future<Peer> future = executor.submit(task);
            results.add(future);
        }

        ArrayList<Peer> peers = new ArrayList<>();
        try {
            for(Future<Peer> future: results) {
                Peer p = future.get();
                if(p != null)
                    peerList.put(p,0);
            }

        } catch (Exception e) {
            System.out.println("Interrupted.");
            e.printStackTrace();
        }

        int a = peerList.size();
        System.out.println("Process is alive with " + a + " peers.");
    }


}