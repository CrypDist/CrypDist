package Blockchain;

import DbManager.PostgresDB;
import UploadUnit.ServerAccessor;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by Kaan on 18-Feb-17.
 */

public class BlockchainManager extends Observable
{
    static transient Logger log = Logger.getLogger("Blockchain");

    private final int BLOCK_SIZE = 4;
    private final int MAX_TIMEOUT_MS = 10000;
    private Blockchain blockchain;
    private PostgresDB dbManager;
    private ServerAccessor serverAccessor;
    private ConcurrentHashMap<String, Pair> transactionPendingBucket;
    private PriorityBlockingQueue<Transaction> transactionBucket;
    private ArrayList<Transaction> transactionBucket_solid;
    private boolean hashReceived;
    private String lastHash;
    private Long lastTime;
    private final String TIME_SERVER = "nist1-macon.macon.ga.us";
    // To collect hash values to given blockIds with time stamp
    // Mapping is like BlockId -> ArrayOf[(Hash, TimeStamp)]
    private ConcurrentHashMap<String, ArrayList<Pair>> hashes;
    private int numOfPairs;

    public BlockchainManager()
    {
        Block genesis = new Block();
        dbManager = new PostgresDB("blockchain", "postgres", "", false);
        blockchain = new Blockchain(genesis);
        //     serverAccessor = new ServerAccessor();
        transactionPendingBucket = new ConcurrentHashMap<>();
        transactionBucket = new PriorityBlockingQueue<>();
        transactionBucket_solid = new ArrayList<>(BLOCK_SIZE);
        hashes = new ConcurrentHashMap<>();
        numOfPairs = 0;
        Timer timer = new Timer();
        timer.schedule(new BlockchainBatch(),0, 8000);
        HashValidation validation = new HashValidation();
        validation.start();
    }

    public Blockchain getBlockchain()
    {
        return blockchain;
    }

    public void uploadFile(String filePath)
    {
        String[] path = filePath.split("/");
        String fileName = path[path.length - 1];

        Transaction upload = new Transaction(fileName, filePath);
        //upload.execute(serverAccessor);

        Gson gson = new Gson();

        log.trace(gson.toJson(upload));
        transactionPendingBucket.put(gson.toJson(upload), new Pair<Transaction, Integer>(upload, 0));
        log.info("Transaction added, being broadcasted.");
        broadcast(gson.toJson(upload), 1, null);

        log.info("Notified");
    }

    // Transaction is serializable and taken from the p2p connection as it is
/*    public void addTransaction(Transaction transaction)
    {
        transactionBucket.add(transaction);
    }
*/
    public void addTransaction(String data)
    {
        Gson gson = new Gson();
        Transaction transaction = gson.fromJson(data, Transaction.class);
        transactionBucket.add(transaction);
        System.out.println("My bucket size is:" + transactionBucket.size());
    }

    private boolean addBlockToBlockchain(Block block) throws Exception {
        Gson gson = new Gson();
        if (blockchain.addBlock(block))
            if (dbManager.addBlock(block.getHash(), gson.toJson(block, Block.class)))
                return true;

        try {
            throw new Exception("The block is added to the blockchain but the db could not be updated!");
        }
        catch (Exception ignored){}

        return false;
    }


    public void receiveHash(String data, Long timeStamp, String blockId) {
        hashReceived = true;
        lastHash = data;
        lastTime = timeStamp;
        if (!hashes.containsKey(blockId))
            hashes.put(blockId, new ArrayList<>());
        hashes.get(blockId).add(new Pair<String, Long>(lastHash, timeStamp));
    }

    public int getBlockchainLength()
    {
        return blockchain.getLength();
    }

//    public ArrayList<Block> getLongestChain()
//    {
//        ArrayList<String> hashChain = blockchain.getBlockchain();
//        ArrayList<Block> blocks = new ArrayList<Block>();
//
//        for (int i = 0; i < hashChain.size(); i++)
//            blocks.add(blockchain.getBlock(hashChain.get(i)));
//        return blocks;
//    }

    public void createBlock()
    {
        log.trace("Block is being created");
        if (transactionBucket_solid.size() != BLOCK_SIZE)
        {
            return;
        }
        else
        {
            String prevHash = blockchain.getLastBlock();
            long timestamp = getTime();
            long maxNonce = Long.MAX_VALUE;

            String blockId = generateBlockId(transactionBucket_solid);
            if (!hashes.containsKey(blockId))
                hashes.put(blockId, new ArrayList<>());

            String hash = mineBlock(blockId, prevHash, timestamp, maxNonce);
            hashReceived = false;

            Block block = null;
            try {
                log.trace("Hash in block: " + hash);
                block = new Block(prevHash, timestamp, hash, transactionBucket_solid, blockchain);
                if (block == null)
                {
                    log.fatal("BLOCK COULD NOT BE CREATED");
                    return;
                }
                for (Transaction t : block.getTransactions()) {
                    transactionBucket.remove(t);
                    transactionBucket_solid.remove(t);
                }

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            try {
                addBlockToBlockchain(block);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String generateBlockId(ArrayList<Transaction> transactionBucket_solid) {
        StringBuilder blockId = new StringBuilder();
        for (Transaction t : transactionBucket_solid)
            blockId.append(t.getStringFormat());
        return blockId.toString();
    }

    public long getTime()
    {
        long timeL = 0;
        NTPUDPClient timeClient = new NTPUDPClient();
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getByName(TIME_SERVER);
            TimeInfo timeInfo = timeClient.getTime(inetAddress);
            timeL = timeInfo.getMessage().getTransmitTimeStamp().getTime();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return timeL;
    }

    public boolean validateHash(String hash)
    {
        return blockchain.getLastBlock().equals(hash);
    }

    public String mineBlock(String blockId, String prevHash, long timestamp, long maxNonce)
    {
        ExecutorService executor = Executors.newCachedThreadPool();

        Callable<String> task =  new BlockMiner(blockId, prevHash, timestamp, maxNonce, transactionBucket_solid);
        Future<String> future = executor.submit(task);

        try {
            return future.get();
        } catch (Exception e) {
            return "";
        }
    }

    // Any message which is going to be broadcasted will be processed in here
    public Long broadcast(String data, int flag, String blockId)
    {
        long time = 0;
        JsonObject obj = new JsonObject();
        obj.addProperty("flag",flag);
        obj.addProperty("data", data);
        if (flag == 2 ) {
            obj.addProperty("blockId", blockId);
            time = getTime();
            obj.addProperty("timeStamp", time);
        }
        setChanged();
        notifyObservers(obj);

        return time;
    }

    public void markValid(String transaction)
    {
        if(transactionPendingBucket.containsKey(transaction)) {
            int count = (int)transactionPendingBucket.get(transaction).scnd;
            transactionPendingBucket.get(transaction).scnd = count + 1;
            if (count + 1 > numOfPairs / 2)
            {
                Transaction tr = ((Transaction)transactionPendingBucket.get(transaction).frst);
                transactionBucket.add(tr);
                transactionPendingBucket.remove(transaction);
            }
            System.out.println("COUNT=\t" + (count + 1));
            System.out.println("PAIR NUM=\t" + numOfPairs);
        }
    }

    /**
     * This class is used for mining a block which means finding a
     * convenient hash key before creating it. The hash key must
     * include zeros in its 8 most significant digit. The purpose is
     * to find a minimum value which is called nonce to make the hash
     * key. The score should be below a target after doing this.
     */
    private class BlockMiner implements Callable<String>
    {
        private MerkleTree data;
        private String prevHash;
        private long timestamp;
        private long maxNonce;
        private String blockId;

        public BlockMiner(String blockId, String prevHash, long timestamp, long maxNonce,
                          ArrayList<Transaction> transactions)
        {
            this.blockId = blockId;
            this.prevHash = prevHash;
            this.timestamp = timestamp;
            this.maxNonce = maxNonce;

            ArrayList<String> stringTransactions = new ArrayList<String>();
            for (int i = 0; i < transactions.size(); i++)
                stringTransactions.add(transactions.get(i).getStringFormat());
            data = new MerkleTree(stringTransactions);
        }

        public String call()
        {
/*            Random rnd = new Random();
            int sleepAmount = rnd.nextInt(200);
            try {
                Thread.sleep(sleepAmount + 300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            */
            long score = Long.MAX_VALUE;
            long bestNonce = -1;
            byte[] hash = null;
            try
            {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                String blockData = "{" + timestamp + ":" + prevHash + ":" +
                        data.getRoot();

                // Try all values as brute-force
                for (int i = 0; i < maxNonce; i++)
                {
                    if(hashes.get(blockId).size() > numOfPairs/2) {
    //                    Thread.currentThread().interrupt();
                        System.out.println("THEY CAME BEFORE I PRODUCE");
                        System.out.println("HASH_NUM_I_HAVE = " + hashes.get(blockId).size());
                        System.out.println("GREATER THAN = " + numOfPairs/2);
                        String minHash = findMinHash(blockId);
                        return minHash;
                    }
                    String dataWithNonce = blockData + ":" + i + "}";
                    hash = md.digest(dataWithNonce.getBytes("UTF-8"));

                    long tempScore = 0L;
                    for (int j = 0; j < 8; j++)
                        tempScore = (hash[j] & 0xff) + (tempScore << 8);

                    if (tempScore < score && tempScore > 0)
                        score = tempScore;

                    // Check if most significant 8 digits are zero
                    if ((hash[0] & 0xff) == 0x00)
                    {
                        bestNonce = i;
                        break;
                    }
                }
            }
            catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {}

            log.trace("CALL TO NOTIFY OBSERVERS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            log.trace("Produced Hash=\t" + new String(hash));

            long timeStamp = broadcast(new String(hash), 2, blockId);

            hashes.get(blockId).add(new Pair<String, Long>(new String(hash), timeStamp));

            String minHash = findMinHash(blockId);

            return minHash;
        }

        private String findMinHash(String blockId)
        {
            while (hashes.get(blockId).size() < numOfPairs/2 + 1) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("DAMDAMDAMDAMDAMDAMDAMDAMDAMDAM");
            }

            long minTime = Long.MAX_VALUE;
            String minHash = "";
            for (Pair p : hashes.get(blockId)){
                log.trace("HASH:\t" + p.frst);
                if ((long)p.scnd < minTime)
                {
                    minTime = (long) p.scnd;
                    minHash = (String) p.frst;
                }
            }
            System.out.println("Chosen hash= " + minHash);
            hashes.remove(blockId);
            return minHash;
        }
    }

    private class BlockchainBatch extends TimerTask{
        @Override
        public void run() {
            while(true) {
                if(!transactionBucket.isEmpty()) {
                    if (transactionBucket.peek().getTimeStamp() < getTime() - MAX_TIMEOUT_MS)
                    {
                        transactionBucket_solid.add(transactionBucket.poll());
                        if (transactionBucket_solid.size() == BLOCK_SIZE)
                        {
                            hashReceived = false;
                            createBlock();
                            hashReceived = false;
                        }
                    }
                    else
                        break;
                }

            }
        }
    }

    private class Pair<T,V>{
        T frst;
        V scnd;

        public Pair(T frst, V scnd)
        {
            this.frst = frst;
            this.scnd = scnd;
        }
    }

    private class HashValidation extends Thread{

        public void run()
        {
            while(true)
            {
                Set<String> keys = transactionPendingBucket.keySet();
                for (String key : keys)
                {
                    Pair pair = transactionPendingBucket.get(key);
                    Transaction trans = (Transaction)pair.frst;
                    if (trans.getTimeStamp() < getTime() - 1000)
                    {
                        JsonObject obj = new JsonObject();
                        obj.addProperty("flag", 4);

                        BlockchainManager.this.setChanged();
                        BlockchainManager.this.notifyObservers(obj);
                    }
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setNumOfPairs(int numOfPairs) {
        this.numOfPairs = numOfPairs;
    }
}
