
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Blockchain implements Serializable
{
    private HashMap<String, Block> blockMap;
    private ArrayList<String> sinkBlocks;
    private Block validBlock;

    public Blockchain(Block genesis)
    {
        blockMap = new HashMap<String, Block>();
        sinkBlocks = new ArrayList<String>();
        validBlock = genesis;
        blockMap.put(genesis.getHash(), genesis);
        sinkBlocks.add(genesis.getHash());
    }

    public void updateConsensus()
    {
        int longest = 0;
        for (int i = 0; i < sinkBlocks.size(); i++)
        {
            String tmp = sinkBlocks.get(i);
            Block tmpBlock = blockMap.get(tmp);
            if (tmpBlock.getLength() > longest)
            {
                longest = tmpBlock.getLength();
                validBlock = tmpBlock;
            }
        }
    }

    public void removeOldBlocks()
    {
        for (int i = 0; i < sinkBlocks.size(); i++)
        {
            Block sink = getBlock(sinkBlocks.get(i));
            if (sink != validBlock && sink.getLength() <= validBlock.getLength() / 2)
            {
                Block curr = sink;
                while (curr.getIndegree() == 0)
                {
                    Block prev = getBlock(sink.getPreviousHash());
                    prev.decrementIndegree();
                    blockMap.remove(curr.getHash());
                    sinkBlocks.remove(curr);
                    curr = prev;
                }
            }
        }
    }

    public int getLength()
    {
        return validBlock.getLength();
    }

    public Block getBlock(String hash)
    {
        return blockMap.get(hash);
    }

    public boolean addBlock(Block block)
    {
        if (!block.validateBlock())
            return false;

        for (int i = 0; i < sinkBlocks.size(); i++)
        {
            String tmp = sinkBlocks.get(i);
            Block tmpBlock = blockMap.get(tmp);
            if (block.getHash().equals(tmpBlock.getHash()))
                return false;
            if (block.getPreviousHash().equals(tmpBlock.getHash()))
            {
                blockMap.put(block.getHash(), block);
                sinkBlocks.remove(tmpBlock);
                sinkBlocks.add(block.getHash());
                tmpBlock.incrementIndegree();
                updateConsensus();
                removeOldBlocks();
                return true;
            }
        }
        return false;
    }

    public String getLastBlock()
    {
        return validBlock.getHash();
    }
}
