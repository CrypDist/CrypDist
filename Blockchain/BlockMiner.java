import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class is used for mining a block which means finding a 
 * convenient hash key before creating it. The hash key must 
 * include zeros in its 8 most significant digit. The purpose is 
 * to find a minimum value which is called nonce to make the hash 
 * key. The score should be below a target after doing this. 
 */
public class BlockMiner 
{
	private long blockId;
	private MerkleTree data;
	private String prevHash;
	private long timestamp;
	private int maxNonce;
	
	public BlockMiner(String rawBlock)
	{
		// parse input
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
			
			// try all values as brute-force
			for (int i = 0; i < maxNonce; i++)
			{
				String dataWithNonce = blockData + ":" + i + "}";
				byte[] hash = md.digest(dataWithNonce.getBytes("UTF-8"));
				// check if most significant 8 digits are zero
				long tempScore = 0L;
				for (int j = 0; j < 8; j++)
					tempScore = (hash[j] & 0xff) + (tempScore << 8);
				
				if (tempScore < score && tempScore > 0)
				{
					score = tempScore;
					bestNonce = i;
				}
			}
		}
		catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {} 
		return bestNonce + ":" + score;
	}
}
