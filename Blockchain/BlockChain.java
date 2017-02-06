import java.util.ArrayList;
import java.util.HashMap;

public class BlockChain 
{
	private HashMap<String, Block> blockMap;
	private ArrayList<ArrayList<String>> chains;
	private boolean gotGenesisBlock;
	
	public BlockChain()
	{
		chains = new ArrayList<ArrayList<String>>();
		gotGenesisBlock = false;
	}
	
	public int getBlockChainLength()
	{
		int longestChain = 0;
		for (int i = 0; i < chains.size(); i++)
		{
			if (chains.get(i).size() > longestChain)
				longestChain = chains.get(i).size();
		}
		return longestChain;
	}
	
	public Block getLatestBlock()
	{
		int length = getBlockChainLength();
		for (int i = 0; i < chains.size(); i++)
			if (chains.get(i).size() == length)
				return blockMap.get(chains.get(i).get(length - 1));
		return null;
	}
	
	public void addBlock(Block block)
	{
		int largestLength = getBlockChainLength();
		for (int i = 0; i < chains.size(); i++)
		{
			if (chains.get(i).size() < largestLength - 10)
			{
				chains.remove(i);
				i--;
			}
		}
		
		if (!gotGenesisBlock)
		{
			gotGenesisBlock = true;
			chains.add(new ArrayList<String>());
			chains.get(0).add(block.getHash());
			blockMap.put(block.getHash(), block);
			return;
		}
		
		for (int i = 0; i < chains.size(); i++)
		{
			String lastBlockHash = chains.get(i).get(chains.get(i).size() - 1);
			if (lastBlockHash.equals(block.getHash()))
				return;
		}
		
		for (int i = 0; i < chains.size(); i++)
		{
			String lastBlockHash = chains.get(i).get(chains.get(i).size() - 1);
			if (lastBlockHash.equals(block.getPreviousHash()))
			{
				chains.get(i).add(block.getHash());
				blockMap.put(block.getHash(), block);
				return;
			}
		}
		
		for (int i = 0; i < chains.size(); i++)
		{
			ArrayList<String> tmpChain = chains.get(i);
			for (int j = tmpChain.size() - 11; j < tmpChain.size(); j++)
			{
				if (j < 0) j = 0;
				
				if (tmpChain.get(j).equals(block.getPreviousHash()))
				{
					ArrayList<String> newChain = new ArrayList<String>();
					for (int k = 0; k <= j; k++)
						newChain.add(tmpChain.get(k));
					newChain.add(block.getHash());
					blockMap.put(block.getHash(), block);
					j = tmpChain.size();
					i = chains.size();
				}
			}
		}
	}
}
