package Blockchain;

import DbManager.PostgresDB;
import UploadUnit.ServerAccessor;
import Util.Config;
import Util.CrypDist;
import Util.Decryption;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URL;
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
    static transient Logger log = Logger.getLogger("BlockchainManager");
    private CrypDist crypDist;
    private final int BLOCK_SIZE = 4;
    private final int MAX_TIMEOUT_MS = Config.BLOCKCHAIN_BATCH_TIMEOUT;
    private Blockchain blockchain;
    private PostgresDB dbManager;
    private ServerAccessor serverAccessor;
    private ConcurrentHashMap<String, Transaction> transactionPendingBucket;
    private PriorityBlockingQueue<Transaction> transactionBucket;
    private ArrayList<Transaction> transactionBucket_solid;
    private final String TIME_SERVER = "nist1-macon.macon.ga.us";
    private Long serverTime;
    private Long systemTime;
    // To collect hash values to given blockIds with time stamp
    // Mapping is like BlockId -> ArrayOf[(Hash, TimeStamp)]
    private ConcurrentHashMap<String, ArrayList<Pair>> hashes;
    private int numOfPairs;
    private boolean updating;

    public BlockchainManager(CrypDist crypDist, byte[] session_key)
    {
        this.crypDist = crypDist;
        dbManager = new PostgresDB("blockchain", "postgres", "", false);
        serverAccessor = new ServerAccessor();
        transactionPendingBucket = new ConcurrentHashMap<>();
        transactionBucket = new PriorityBlockingQueue<>();
        transactionBucket_solid = new ArrayList<>(BLOCK_SIZE);
        buildBlockchain();
        hashes = new ConcurrentHashMap<>();
        numOfPairs = 0;
        serverTime = getServerTime();
        systemTime = System.currentTimeMillis();
        updating = false;
        Timer timer = new Timer();
        timer.schedule(new BlockchainBatch(),0, Config.BLOCKCHAIN_BATCH_PERIOD);
    }

    public void buildBlockchain()
    {
        Gson gson = new Gson();
        Blockchain blockchainDB = gson.fromJson(dbManager.getBlockchain(), Blockchain.class);
        if (blockchainDB != null)
            blockchain = blockchainDB;
        else{
            Block genesis = new Block();
            blockchain = new Blockchain(genesis);
        }
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


    public void uploadFile(String filePath, String dataSummary) throws Exception {

        if(!crypDist.isAuthenticated())
            return;

        log.warn("FILE PATH CAME AS :" + filePath);
        String[] path = filePath.substring(1).split("/");
        String fileName = path[path.length - 1];

        File file = new File(filePath);
        if(fileName.equals("merhaba") || (file.exists() && !file.isDirectory())) {
            long dataSize = file.length();
            URL url = serverAccessor.getURL(fileName);
            Transaction upload = new Transaction(filePath, fileName, dataSummary, dataSize, url,crypDist.getSessionKey());
            Gson gson = new Gson();

            log.info(gson.toJson(upload));
            transactionPendingBucket.put(gson.toJson(upload), upload);
            log.info("Transaction added, being broadcasted.");
            broadcast(gson.toJson(upload), Config.FLAG_BROADCAST_TRANSACTION, null);

            log.info("Notified");
        }
        else
            throw new Exception("No such file!");

    }

    public void updateFile(Transaction transaction, String filePath, String dataSummary) throws Exception
    {
        if(!crypDist.isAuthenticated())
            return;

        log.warn("FILE PATH CAME AS :" + filePath);
        String[] path = filePath.substring(1).split("/");
        String fileName = path[path.length - 1];

        File file = new File(filePath);
        if(fileName.equals("merhaba") || (file.exists() && !file.isDirectory())) {
            long dataSize = file.length();
            URL url = serverAccessor.getURL(fileName);
            Transaction upload = new Transaction(filePath, fileName, dataSummary, dataSize, url,crypDist.getSessionKey(),
                                                 transaction.getVersion() + 1);
            Gson gson = new Gson();

            log.info(gson.toJson(upload));
            transactionPendingBucket.put(gson.toJson(upload), upload);
            log.info("Transaction added, being broadcasted.");
            broadcast(gson.toJson(upload), Config.FLAG_BROADCAST_TRANSACTION, null);

            log.info("Notified");
        }
        else
            throw new Exception("No such file!");
    }

    public void downloadFile(String fileName, String path)
    {
        try {
            serverAccessor.download(fileName,path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addTransaction(String data, String ip)
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
            hashes.get(blockId).add(new Pair<>(data, timeStamp));
            log.info("the hash is added to the hashes");
        }
    }

    public void createBlock()
    {
        log.info("Block is being created");

        String prevHash = blockchain.getLastBlock();
        long timestamp = getTime();
        long maxNonce = Long.MAX_VALUE;

        String blockId = generateBlockId(transactionBucket_solid);
        synchronized (this) {
            if (!hashes.containsKey(blockId))
                hashes.put(blockId, new ArrayList<>());
        }
        String hash = mineBlock(blockId, prevHash, timestamp, maxNonce);

        Block block = null;
        try {
            synchronized (this) {
                log.info("Hash in block: " + hash);
                block = new Block(prevHash, timestamp, hash, transactionBucket_solid, blockchain);
                for (Transaction t : block.getTransactions()) {
                    transactionBucket.remove(t);
                    transactionBucket_solid.remove(t);
                }
            }
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    try {
            addBlockToBlockchain(block);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String generateBlockId(ArrayList<Transaction> transactionBucket_solid) {
        StringBuilder blockId = new StringBuilder();
        for (Transaction t : transactionBucket_solid)
            blockId.append(t.getStringFormat());
        return blockId.toString();
    }

    public long getServerTime()
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

    private long getTime()
    {
        return serverTime + System.currentTimeMillis() - systemTime;
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

        if (flag == Config.FLAG_BROADCAST_HASH ) {
            obj.addProperty("blockId", blockId);
            time = getTime();
            obj.addProperty("timeStamp", time);
        }

        crypDist.updateByBlockchain(obj);

        return time;
    }

    public String getKeySet() {
        Set<String> keys = blockchain.getKeySet();
        if (keys.size() == 0)
            return "";

        Gson gson = new Gson();
        return gson.toJson(keys);
    }

    public Block getBlock(String hash)
    {
        Block block = blockchain.getBlock(hash);
        return block;
    }

    public void markValid(String transaction)
    {
        synchronized (this) {
            if (transactionPendingBucket.containsKey(transaction)) {
                Transaction tr = transactionPendingBucket.get(transaction);
                transactionBucket.add(tr);
                transactionPendingBucket.remove(transaction);
                if (!tr.getFileName().equals("merhaba"))
                    tr.execute(serverAccessor);
                log.info("VALIDATED");
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

            Random rnd = new Random();
            try {
                Thread.sleep(rnd.nextInt(100));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long timeStamp = broadcast(new String(hash), Config.FLAG_BROADCAST_HASH, blockId);

            hashes.get(blockId).add(new Pair<String, Long>(new String(hash), timeStamp));

            String minHash = findMinHash(blockId);

            return minHash;
        }

        private String findMinHash(String blockId)
        {
            while (hashes.get(blockId).size() < numOfPairs/2 + 1) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            long minTime = Long.MAX_VALUE;
            String minHash = "";

            synchronized (this) {
                for (Pair p : hashes.get(blockId)) {
                    if ((long) p.scnd < minTime) {
                        minTime = (long) p.scnd;
                        minHash = (String) p.frst;
                    }
                }
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

                if (!transactionBucket_solid.isEmpty())
                {
                    if (transactionBucket_solid.get(0).getTimeStamp() < getTime() - Config.BLOCK_CREATION_TIMEOUT)
                    {
                        createBlock();
                    }
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


    // TODO This class can be used to trace the hashes taken but not yet started to the respective block mining
//    private class HashValidation extends Thread{
//
//        public void run()
//        {
//            while(true)
//            {
//                if (!updating) {
//                    Set<String> keys = transactionPendingBucket.keySet();
//                    for (String key : keys) {
//                        Transaction trans = transactionPendingBucket.get(key);
//                        if (trans.getTimeStamp() < getTime() - Config.TRANSACTION_VALIDATION_TIMEOUT) {
//                            log.warn("UPDATE THEM!!!!!!!!!!!!!!!!!!!");
//                            updating = true;
//                            crypDist.updateBlockchain();
//                        }
//                    }
//
//                    try {
//                        Thread.sleep(100);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//    }

    public void setUpdating(boolean updating)
    {
        this.updating = updating;
    }

    public void setNumOfPairs(int numOfPairs) {
        this.numOfPairs = numOfPairs;
    }

    public Set<String> getNeededBlocks(Set<String> keySet)
    {
        return blockchain.getNeededBlocks(keySet);
    }

    public Set<String> getPurifiedList(ArrayList<String> keySet)
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
        return resultingList;
    }

    public void addNewBlocks(HashMap<String, String> blocks)
    {
        Gson gson = new Gson();

        String lastHash = blockchain.getLastBlock();

        log.info("Size of adding: " + blocks.size());
        log.info("1.Blockchain size is: " + blockchain.getLength());
        log.info("1.Blockchain lasthash: " + blockchain.getLastBlock());
        log.info("blocks size is: " + blocks.size());
        while (blocks.size() > 0) {

            Iterator<String> iterator = blocks.keySet().iterator();
            while    (iterator.hasNext())
            {
                log.info("in iterator!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                String key = iterator.next();
                log.info("KEY " + key);

                Block block = gson.fromJson(blocks.get(key), Block.class);
                String prevHash = block.getPreviousHash();

                if (prevHash.equals(lastHash))
                {
                    log.info("PREV HASH EQUALS TO THE LAST HASH");
                    blocks.remove(key);
                    lastHash = block.getHash();
                    try {
                        boolean added = addBlockToBlockchain(block);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }

        }

        log.info("New blockchain size is: " + blockchain.getLength());
        log.info("New blockchain lasthash: " + blockchain.getLastBlock());
        transactionPendingBucket.clear();
        transactionBucket.clear();
        transactionBucket_solid.clear();
    }


    public void removeInvalidBlocks(ArrayList<String> keySet)
    {
        blockchain.removeInvalidBlocks(keySet);
    }
}
