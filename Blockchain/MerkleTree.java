import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Tree that holds transaction in a block
 * Leaves hold the transaction signatures which are constructed by the SHA-512
 * algorithm from the string representations
 * Signature of the root is kept in the block to verify the transactions
 */

public class MerkleTree 
{
	public static final int LEAF_SIG_TYPE = 0x0;
	public static final int INTERNAL_SIG_TYPE = 0x01;
	
	private List<String> leafSigs;
	private Node root;
	private int depth;
	private int nrNodes;
	
	public MerkleTree(List<String> leafSigs)
	{
		constructTree(leafSigs);
	}
	
	private void constructTree(List<String> leafSigs)
	{
		this.leafSigs = leafSigs;
		nrNodes = leafSigs.size();
		List<Node> parents = bottomLevel(leafSigs);
		nrNodes += parents.size();
		depth = 1;
		
		while (parents.size() > 1)
		{
			parents = internalLevel(parents);
			depth++;
			nrNodes += parents.size();
		}
		
		root = parents.get(0);
	}
	
	public List<Node> internalLevel(List<Node> children)
	{
		List<Node> parents = new ArrayList<Node>();
		
		for (int i = 0; i < children.size() - 1; i += 2)
		{
			Node child1 = children.get(i);
			Node child2 = children.get(i + 1);
			Node parent = constructInternalNode(child1, child2);
			parents.add(parent);
		}
		
		if (children.size() % 2 != 0)
		{
			Node child = children.get(children.size() - 1);
			Node parent = constructInternalNode(child, null);
			parents.add(parent);
		}
		
		return parents;
	}
	
	public List<Node> bottomLevel(List<String> leafSigs)
	{
		List<Node> parents = new ArrayList<Node>();
		
		for (int i = 0; i < leafSigs.size() - 1; i += 2)
		{
			Node leaf1 = constructLeafNode(leafSigs.get(i));
			Node leaf2 = constructLeafNode(leafSigs.get(i + 1));
			Node parent = constructInternalNode(leaf1, leaf2);
			parents.add(parent);
		}
		
		if (leafSigs.size() % 2 != 0)
		{
			Node leaf = constructLeafNode(leafSigs.get(leafSigs.size() - 1));
			Node parent = constructInternalNode(leaf, null);
			parents.add(parent);
		}
		
		return parents;
	}
	
	private Node constructInternalNode(Node child1, Node child2)
	{
		Node parent = new Node();
		parent.type = INTERNAL_SIG_TYPE;
		
		if (child2 == null)
			parent.signature = child1.signature;
		else
			parent.signature = internalHash(child1.signatureToString(), child2.signatureToString());
		
		parent.left = child1;
		parent.right = child2;
		return parent;
	}
	
	private Node constructLeafNode(String signature)
	{
		Node leaf = new Node();
		leaf.type = LEAF_SIG_TYPE;
		signature = SHA512(signature);
		leaf.signature = signature.getBytes(StandardCharsets.UTF_8);
		return leaf;
	}
	
	// compute internal hash by concatenating the child signatures
	private byte[] internalHash(String leftChildSignature, String rightChildSignature)
	{
		String hash = SHA512(leftChildSignature + rightChildSignature);
		return hash.getBytes(StandardCharsets.UTF_8);
	}
	
	public int getDepth()
	{
		return depth;
	}
	
	public String getRoot()
	{
		return root.signatureToString();
	}
	
	private String SHA512(String data)
	{
		String signature = null;
		try
		{	
//			String alphabet = "abcdefghijklmnopqrstuvyz123456789";
//			String salt = "";
//			Random rand = new Random();
//			for (int i = 0; i < 10; i++)
//			{
//				int index = rand.nextInt(alphabet.length());
//				salt += alphabet.charAt(index);
//			}
			
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			md.update(data.getBytes("UTF-8"));
			byte[] bytes = md.digest(data.getBytes("UTF-8"));
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < bytes.length; i++)
				sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
			signature = sb.toString();
		}
		catch (NoSuchAlgorithmException | UnsupportedEncodingException e)
		{
	        e.printStackTrace();
	    }
		return signature;
	}
	
	private class Node
	{
		public byte type;
		public byte[] signature;
		public Node left;
		public Node right;
		
		public String signatureToString()
		{
			StringBuffer sb = new StringBuffer();
			sb.append('[');
			
			for (int i = 0; i < signature.length; i++)
				sb.append(signature[i]).append(' ');
			sb.insert(sb.length() - 1, ']');
			return sb.toString();
		}
	}
}
