import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.xml.bind.DatatypeConverter;

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
	private MerkleTree data;
	
	// header information
	private String hash;
	private String prevHash;
	private String merkleRoot;
	private long timestamp;
	private int nonce;
	private long targetDifficulty;
	
	public Block(long id, String prevHash, long timestamp, int nonce, long targetDifficulty, 
				 ArrayList<String> transactions) throws NoSuchAlgorithmException, 
														UnsupportedEncodingException
	{
		this.id = id;
		this.prevHash = prevHash;
		this.timestamp = timestamp;
		this.nonce = nonce;
		this.targetDifficulty = targetDifficulty;
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
		MessageDigest md = MessageDigest.getInstance("SHA-512");
		String blockData = "{" + timestamp + ":" + id + ":" + prevHash + ":" + nonce 
							+ ":" + data.getRoot() + "}";
		return DatatypeConverter.printHexBinary(md.digest(blockData.getBytes("UTF-8")));
	}
	
	public long computeDifficulty()
	{
		// TODO
		return 0;
	}
	
	public long getTargetDifficulty()
	{
		return targetDifficulty;
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
}
