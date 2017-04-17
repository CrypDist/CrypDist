
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.TimerTask;
import java.util.concurrent.*;

/**
 * Created by Kaan on 18-Feb-17.
 */

public class BlockchainManager extends Observable
{
    private final int BLOCK_SIZE = 4;
    private final int MAX_TIMEOUT_MS = 10000;
    private Blockchain blockchain;
    private PostgresDB dbManager;
    private ServerAccessor serverAccessor;
    private PriorityBlockingQueue<Transaction> transactionBucket;
    private ArrayList<Transaction> transactionBucket_solid;
    private boolean hashReceived;
    private String lastHash;

    public BlockchainManager()
    {
        Block genesis = new Block();
        dbManager = new PostgresDB("blockchain", "postgres", "", false);
        blockchain = new Blockchain(genesis);
        serverAccessor = new ServerAccessor();
        transactionBucket = new PriorityBlockingQueue<>();
        transactionBucket_solid = new ArrayList<>(BLOCK_SIZE);
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
        upload.execute(serverAccessor);
        transactionBucket.add(upload);


        Gson gson = new Gson();
        JsonObject obj = new JsonObject();
        obj.addProperty("flag",1);
        obj.addProperty("data", gson.toJson(upload));
        notifyObservers(obj);

    }

    // Transaction is serializable and taken from the p2p connection as it is
    public void addTransaction(Transaction transaction)
    {
        transactionBucket.add(transaction);
    }
    public void addTransaction(String data)
    {
        Gson gson = new Gson();
        Transaction transaction = gson.fromJson(data, Transaction.class);
        transactionBucket.add(transaction);
    }

    private boolean addBlockToBlockchain(Block block) throws Exception {
        if (blockchain.addBlock(block))
            if (dbManager.addBlock(block.getHash(), block.toString()))
                return true;

        try {
            throw new Exception("The block is added to the blockchain but the db could not be updated!");
        }
        catch (Exception ignored){}

        return false;
    }


    public void receiveHash(String data) {
        Gson gson = new Gson();
        String str = gson.fromJson(data,String.class);

        hashReceived = true;
        lastHash = str;
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
        if (transactionBucket_solid.size() != BLOCK_SIZE)
        {
            return;
        }
        else
        {
            String prevHash = blockchain.getLastBlock();
            long timestamp = getTime();
            long maxNonce = Long.MAX_VALUE;
            String hash = mineBlock(prevHash, timestamp, maxNonce);
            Block block = null;
            try {
                block = new Block(prevHash, timestamp, hash, transactionBucket_solid, blockchain);
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

    public long getTime()
    {
        return System.currentTimeMillis();
    }

    public String mineBlock(String prevHash, long timestamp, long maxNonce)
    {
        ExecutorService executor = Executors.newCachedThreadPool();

        Callable<String> task =  new BlockMiner(prevHash, timestamp, maxNonce, transactionBucket_solid);
        Future<String> future = executor.submit(task);

        try {
            return future.get();
        } catch (Exception e) {
            return "";
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
        private long blockId;
        private MerkleTree data;
        private String prevHash;
        private long timestamp;
        private long maxNonce;

        public BlockMiner(String prevHash, long timestamp, long maxNonce,
                          ArrayList<Transaction> transactions)
        {
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
            if(hashReceived) {
                return lastHash;
            }
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
                    // TODO: OGUZ, BURADA EGER BASKA BIR HASH BULUNMUSSA BU THREAD DURACAK.
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


            Gson gson = new Gson();
            JsonObject obj = new JsonObject();
            obj.addProperty("flag",2);
            obj.addProperty("data", new String(hash));
            notifyObservers(obj);


            return new String(hash);
        }
    }

    public class BlockchainBatch extends TimerTask{
        @Override
        public void run() {
            while(true) {
                if (transactionBucket.peek().getTimeStamp().getTime() < getTime() - MAX_TIMEOUT_MS)
                {
                    transactionBucket_solid.add(transactionBucket.poll());
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
