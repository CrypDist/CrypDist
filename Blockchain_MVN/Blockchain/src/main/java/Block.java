
import javax.xml.bind.DatatypeConverter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * A block in the chain
 * Hash: Unique key of the block (Computed based on all data of the block)
 * Previous hash: Points to the previous block
 * Merkle root: Root signature of merkle tree to check the validity of transactions
 * Timestamp: Creation time
 * Nonce: Number of attempts to produce a hash key with four leading zeros
 * Target difficulty: Maximum number of difficulty to produce a hash key
 *
 * */

public class Block implements Serializable
{
    private static final long serialVersionUID = 1L;
    private long id;
    private ArrayList<String> transactions;
    private MerkleTree data;

    // header information
    private String hash;
    private String prevHash;
    private String merkleRoot;
    private long timestamp;
    private int nonce;
    private byte[] targetDifficulty;

    public Block(long id, String prevHash, long timestamp, int nonce, byte[] targetDifficulty,
                 ArrayList<String> transactions) throws NoSuchAlgorithmException,
            UnsupportedEncodingException
    {
        this.id = id;
        this.prevHash = prevHash;
        this.timestamp = timestamp;
        this.nonce = nonce;
        this.targetDifficulty = targetDifficulty;
        this.transactions = transactions;
        data = new MerkleTree(transactions);
        merkleRoot = data.getRoot();
        hash = computeHash();
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public long getId()
    {
        return id;
    }

    private String computeHash() throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String blockData = "{" + timestamp + ":" + id + ":" + prevHash + ":" + data.getRoot()
                            + ":" + nonce + "}";
        return DatatypeConverter.printHexBinary(md.digest(blockData.getBytes("UTF-8")));
    }

    public long getDifficulty()
    {
        int exponent = targetDifficulty[3];
        int coefficient = targetDifficulty[2] * 256 + targetDifficulty[1] * 16 + targetDifficulty[0];
        long target = coefficient * (int) Math.pow(2, 8 * (exponent - 3));

        if (timestamp < target)
            return target;
        return timestamp;
    }

    public String getHash()
    {
        return hash;
    }

    public String getPreviousHash()
    {
        return prevHash;
    }

    public String getMerkleRoot()
    {
        return merkleRoot;
    }

    public void setPreviousHash(String prevHash)
    {
        this.prevHash = prevHash;
    }

    public boolean isGenesis()
    {
        return (id == 0);
    }

    // Check if block structure is valid
    public boolean validateBlock()
    {
        try
        {
            String transactionString = "";
            for (int i = 0; i < transactions.size(); i++)
                transactionString += transactions.get(i) + "*";

            // Recalculate block hash and compare with the previous one
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String blockData = "{" + timestamp + ":" + id + ":" + prevHash + ":" + data.getRoot()
                                + ":" + nonce + "}";
            String blockHash = DatatypeConverter.printHexBinary(md.digest(
                                        blockData.getBytes("UTF-8")));
            if (hash != blockHash)
                return false;

            // Restructure merkle tree and compare the root with the previous one
            MerkleTree testTree = new MerkleTree(transactions);
            String root = testTree.getRoot();
            if (merkleRoot != root)
                return false;
        }
        catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {}
        return true;
    }
}
