
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * Created by Kaan on 18-Feb-17.
 */
public class BlockchainManager
{
    private Blockchain blockchain;

    public BlockchainManager()
    {
        blockchain = new Blockchain();
    }

    public Blockchain getBlockchain()
    {
        return blockchain;
    }

    public boolean addBlockToBlockchain(Block block)
    {
        return blockchain.addBlock(block);
    }

    public int getBlockchainLength()
    {
        return blockchain.getLength();
    }

    public ArrayList<Block> getLongestChain()
    {
        ArrayList<String> hashChain = blockchain.getBlockchain();
        ArrayList<Block> blocks = new ArrayList<Block>();

        for (int i = 0; i < hashChain.size(); i++)
            blocks.add(blockchain.getBlock(hashChain.get(i)));
        return blocks;
    }

    public String mineBlock(String prevHash, long timestamp, long maxNonce,
                            ArrayList<Transaction> transactions)
    {
        BlockMiner miner = new BlockMiner(prevHash, timestamp, maxNonce, transactions);
        return miner.mineBlock();
    }

    /**
     * This class is used for mining a block which means finding a
     * convenient hash key before creating it. The hash key must
     * include zeros in its 8 most significant digit. The purpose is
     * to find a minimum value which is called nonce to make the hash
     * key. The score should be below a target after doing this.
     */
    private class BlockMiner
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

        public String mineBlock()
        {
            long score = Long.MAX_VALUE;
            int bestNonce = -1;
            try
            {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                String blockData = "{" + timestamp + ":" + blockId + ":" + prevHash + ":" +
                        data.getRoot();

                // Try all values as brute-force
                for (int i = 0; i < maxNonce; i++)
                {
                    String dataWithNonce = blockData + ":" + i + "}";
                    byte[] hash = md.digest(dataWithNonce.getBytes("UTF-8"));

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
            return bestNonce + ":" + score;
        }
    }
}
