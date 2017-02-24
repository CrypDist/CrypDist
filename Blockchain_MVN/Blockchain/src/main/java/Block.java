
import com.sun.deploy.util.SystemUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.log4j.Level;

import javax.xml.bind.DatatypeConverter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.log4j.Logger.*;

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
    private ArrayList<Transaction> transactions;
    private MerkleTree data;
    private byte[] targetDifficulty;
    private String hash;
    private int length;
    private int indegree;

    // header information
    private String prevHash;
    private String merkleRoot;
    private long timestamp;
    private long nonce;

    // genesis block
    public Block()
    {
        length = 0;
        indegree = 0;
        hash = "0x0";
        timestamp = 0L;
        targetDifficulty = new byte[4];
        targetDifficulty[0] = 0;
        targetDifficulty[1] = 0;
        targetDifficulty[2] = 0;
        targetDifficulty[3] = 0;

    }

    public Block(String prevHash, long timestamp, long nonce, byte[] targetDifficulty,
                 ArrayList<Transaction> transactions, Blockchain blockchain) throws NoSuchAlgorithmException,
            UnsupportedEncodingException
    {
        length = blockchain.getBlock(prevHash).getLength() + 1;
        indegree = 0;
        this.prevHash = prevHash;
        this.timestamp = timestamp;
        this.nonce = nonce;
        this.targetDifficulty = targetDifficulty;
        this.transactions = transactions;

        ArrayList<String> stringTransactions = new ArrayList<String>();
        for (int i = 0; i < transactions.size(); i++)
            stringTransactions.add(transactions.get(i).getStringFormat());

        data = new MerkleTree(stringTransactions);
        merkleRoot = data.getRoot();
        hash = computeHash();
    }

    public int getLength()
    {
        return length;
    }

    public int getIndegree()
    {
        return indegree;
    }

    public void incrementIndegree()
    {
        indegree++;
    }

    public void decrementIndegree()
    {
        indegree--;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public long getNonce()
    {
        return nonce;
    }

    private String computeHash() throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String blockData = "{" + timestamp + ":" + prevHash + ":" + data.getRoot()
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
        return (length == 0);
    }

    public ArrayList<Transaction> getTransactions()
    {
        return transactions;
    }

    public long getSerialVersionUID()
    {
        return serialVersionUID;
    }

    // Check if block structure is valid
    public boolean validateBlock()
    {
        try
        {
            // Recalculate block hash and compare with the previous one
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String blockData = "{" + timestamp + ":" + prevHash + ":" + data.getRoot()
                                + ":" + nonce + "}";
            String blockHash = DatatypeConverter.printHexBinary(md.digest(
                                        blockData.getBytes("UTF-8")));
            if (!hash.equals(blockHash))
                return false;

            // Restructure merkle tree and compare the root with the previous one
            ArrayList<String> stringTransactions = new ArrayList<String>();
            for (int i = 0; i < transactions.size(); i++)
                stringTransactions.add(transactions.get(i).getStringFormat());
            MerkleTree testTree = new MerkleTree(stringTransactions);
            String root = testTree.getRoot();
            if (!merkleRoot.equals(root))
                return false;

            // TODO check timestamp

            for (int i = 0; i < transactions.size(); i++)
                if (!transactions.get(i).validate())
                    return false;
        }
        catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {}
        return true;
    }

    // Transform the block to JSON String Notation
    public String serializeBlock() {
        getRootLogger().setLevel(Level.OFF);
        JSONObject jsonObject = JSONObject.fromObject(this);
        return jsonObject.toString();
    }

    // Transform JSON String Notation to JSONObject
    public JSONObject StringToJsonObject(String json) {
        /*
        jsonObject.getLong("difficulty");
        jsonObject.getBoolean("genesis");
        jsonObject.getString("hash");
        jsonObject.getInt("length");
        jsonObject.getString("merkleRoot");
        jsonObject.getLong("nonce");
        jsonObject.getString("previousHash");
        jsonObject.getLong("serialVersionUID");
        jsonObject.getLong("timestamp");
        jsonObject.getJSONArray("transactions");
        */
        JSONObject jsonObject = JSONObject.fromObject(json);
        return jsonObject;
    }

}
