package Blockchain;

import DbManager.PostgresDB;
import UploadUnit.ServerAccessor;
import Util.CrypDist;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
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

public class BlockchainManager
{
    static transient Logger log = Logger.getLogger("Blockchain");
    private CrypDist crypDist;
    private final int BLOCK_SIZE = 4;
    private final int MAX_TIMEOUT_MS = 10000;
    private Blockchain blockchain;
    private PostgresDB dbManager;
    private ServerAccessor serverAccessor;
    private ConcurrentHashMap<String, Pair> transactionPendingBucket;
    private PriorityBlockingQueue<Transaction> transactionBucket;
    private ArrayList<Transaction> transactionBucket_solid;
    private final String TIME_SERVER = "nist1-macon.macon.ga.us";
    // To collect hash values to given blockIds with time stamp
    // Mapping is like BlockId -> ArrayOf[(Hash, TimeStamp)]
    private ConcurrentHashMap<String, ArrayList<Pair>> hashes;
    private int numOfPairs;

    public BlockchainManager(CrypDist crypDist)
    {
        this.crypDist = crypDist;
        Block genesis = new Block();
        dbManager = new PostgresDB("blockchain", "postgres", "", false);
        blockchain = new Blockchain(genesis);
        serverAccessor = new ServerAccessor();
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

    public void buildBlockchain()
    {
        Gson gson = new Gson();
        blockchain = gson.fromJson(dbManager.getBlockchain(), Blockchain.class);
    }

    public void saveBlockchain()
    {
        Gson gson = new Gson();
        dbManager.saveBlockchain(gson.toJson(blockchain, Blockchain.class));
    }

    public Blockchain getBlockchain()
    {
        return blockchain;
    }


    public void uploadFile(String filePath, String dataSummary)
    {
        log.warn("FILE PATH CAME AS :" + filePath);
        String[] path = filePath.substring(1).split("/");
        String fileName = path[path.length - 1];

        File file = new File(filePath);
        long dataSize = file.length();
        Transaction upload = new Transaction(fileName, filePath, dataSummary, dataSize);

        Gson gson = new Gson();

        log.info(gson.toJson(upload));
        transactionPendingBucket.put(gson.toJson(upload), new Pair<Transaction, Integer>(upload, 0));
        log.info("Transaction added, being broadcasted.");
        broadcast(gson.toJson(upload), 1, null);

        log.info("Notified");
    }

    public void downloadFile(String fileName, String path)
    {
        try {
            serverAccessor.download(fileName,path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addTransaction(String data)
    {
        Gson gson = new Gson();
        Transaction transaction = gson.fromJson(data, Transaction.class);
        transactionBucket.add(transaction);
        System.out.println("My bucket size is:" + transactionBucket.size());
    }

    private boolean addBlockToBlockchain(Block block) throws Exception {
        return blockchain.addBlock(block);
    }


    public void receiveHash(String data, Long timeStamp, String blockId) {
        synchronized (this) {
            if (!hashes.containsKey(blockId)) {
                hashes.put(blockId, new ArrayList<>());
                log.info("The first time a hash is in hashes for the block!");
            }
            hashes.get(blockId).add(new Pair<String, Long>(data, timeStamp));
            log.info("the hash is added to the hashes");
        }
    }

    public void createBlock()
    {
        log.info("Block is being created");
        if (transactionBucket_solid.size() != BLOCK_SIZE)
        {
            log.warn("RETURNED!");
            log.warn("transactionBucket_solid size = " + transactionBucket_solid.size());
            return;
        }
        else
        {
            String prevHash = blockchain.getLastBlock();
            long timestamp = getTime();
            long maxNonce = Long.MAX_VALUE;

            String blockId = generateBlockId(transactionBucket_solid);
            synchronized (this) {
                if (!hashes.containsKey(blockId))
                    hashes.put(blockId, new ArrayList<>());
                log.info("mineBlock is called, hashes size = " + hashes.get(blockId).size());
            }

            String hash = mineBlock(blockId, prevHash, timestamp, maxNonce);

            Block block = null;
            try {
                log.info("Hash in block: " + hash);
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

        crypDist.updateByBlockchain(obj);

        return time;
    }

    public String getKeySet() {
        Set<String> keys = blockchain.getKeySet();
        Gson gson = new Gson();
        return gson.toJson(keys);
    }

    public Block getBlock(String hash)
    {
        Gson gson = new Gson();
        Block block = blockchain.getBlock(hash);
        return block;
    }

    public void markValid(String transaction)
    {
        synchronized (this) {
            if (transactionPendingBucket.containsKey(transaction)) {
                int count = (int) transactionPendingBucket.get(transaction).scnd;
                transactionPendingBucket.get(transaction).scnd = count + 1;
                if (count + 1 > numOfPairs / 2) {
                    Transaction tr = ((Transaction) transactionPendingBucket.get(transaction).frst);
                    transactionBucket.add(tr);
                    transactionPendingBucket.remove(transaction);
                    //tr.execute(serverAccessor);
                }
                System.out.println("COUNT=\t" + (count + 1));
                System.out.println("PAIR NUM=\t" + numOfPairs);
            }
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
                    if (hashes.get(blockId).size() > numOfPairs / 2) {
                        //                    Thread.currentThread().interrupt();
                        System.out.println("THEY CAME BEFORE I PRODUCE");
                        System.out.println("HASH_NUM_I_HAVE = " + hashes.get(blockId).size());
                        System.out.println("GREATER THAN = " + numOfPairs / 2);
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

            log.info("CALL TO NOTIFY OBSERVERS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            log.info("Produced Hash=\t" + new String(hash));

            Random rnd = new Random();
            try {
                Thread.sleep(rnd.nextInt(100));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long timeStamp = broadcast(new String(hash), 2, blockId);

            hashes.get(blockId).add(new Pair<String, Long>(new String(hash), timeStamp));

            String minHash = findMinHash(blockId);

            return minHash;
        }

        private String findMinHash(String blockId)
        {

            log.info("1\t# of hashes: " + hashes.get(blockId).size());
            log.info("1\t# of pairs: " + numOfPairs);

            while (hashes.get(blockId).size() < numOfPairs/2 + 1) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("DAMDAMDAMDAMDAMDAMDAMDAMDAMDAM");
                log.info("# of hashes: " + hashes.get(blockId).size());
                log.info("# of pairs: " + numOfPairs);
            }
            long minTime = Long.MAX_VALUE;
            String minHash = "";

            synchronized (this) {
                for (Pair p : hashes.get(blockId)) {
                    log.info("HASH:\t" + p.frst);
                    if ((long) p.scnd < minTime) {
                        minTime = (long) p.scnd;
                        minHash = (String) p.frst;
                    }
                }
                log.info("Chosen hash= " + minHash);
                hashes.remove(blockId);
                return minHash;
            }
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
                        log.trace("Transaction bucket size = " + transactionBucket.size());
                        if (transactionBucket_solid.size() == BLOCK_SIZE)
                        {
                            createBlock();
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
//                        JsonObject obj = new JsonObject();
//                        obj.addProperty("flag", 4);
//
//                        BlockchainManager.this.setChanged();
//                        BlockchainManager.this.notifyObservers(obj);
                        crypDist.updateBlockchain();
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

    public Set<String> getNeededBlocks(HashSet<String> keySet)
    {
        int size = keySet.size();
        Gson gson = new Gson();

        HashMap<String, Integer> counts = new HashMap<>();

        for(String str : keySet) {
            HashSet<String> singleResponse = gson.fromJson(str,HashSet.class);

            for(String hash: singleResponse){
                if(counts.containsKey(hash)){
                    counts.put(hash, counts.get(hash) + 1);
                }
                else {
                    counts.put(hash, 1);
                }
            }
        }

        HashSet<String> resultingList = new HashSet<>();
        for(Map.Entry<String,Integer> entry : counts.entrySet() ) {
            if(entry.getValue() > size/2) {
                resultingList.add(entry.getKey());
            }
        }

        return blockchain.getNeededBlocks(resultingList);
    }

    public void addNewBlocks(HashMap<String, String> blocks)
    {
        Gson gson = new Gson();

        Set<String> keys = blocks.keySet();
        String lastHash = blockchain.getLastBlock();
        Iterator<String> iterator = keys.iterator();
        String currKey = "";

        while (iterator.hasNext())
        {
            String key = iterator.next();

            Block block = gson.fromJson(blocks.get(key), Block.class);
            String prevHash = block.getPreviousHash();

            if (prevHash.equals(lastHash))
            {
                currKey = key;
                break;
            }
        }

        if (currKey.isEmpty())
            return;

        while (blocks.size() > 0 && currKey != null && blocks.containsKey(currKey))
        {
            log.info("CURRKEY=\t" + currKey);
            log.info("blocks.get=\t" + blocks.get(currKey));
            Block block = null;
            block = gson.fromJson(blocks.get(currKey), Block.class);
            blocks.remove(currKey);
            try {
                boolean added = addBlockToBlockchain(block);
                if (!added)
                    log.warn("ALAAAAAAAAARMMMMMMMMMMMMMMMMM");
            } catch (Exception e) {
                e.printStackTrace();
            }

            currKey = block.getPreviousHash();
        }
    }
}
