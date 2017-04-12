
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

//    // Maps the hashes to blocks
//    private HashMap<String, Block> blockMap;
//    // There are multiple chains for forks
//    private ArrayList<ArrayList<String>> chains;
//    // Valid blockchain according to the longest chain rule
//    private ArrayList<String> longestChain;
//    private ArrayList<Long> difficulties;
//    private boolean gotGenesisBlock;
//
//    public Blockchain()
//    {
//        blockMap = new HashMap<String, Block>();
//        chains = new ArrayList<ArrayList<String>>();
//        longestChain = new ArrayList<String>();
//        difficulties = new ArrayList<Long>();
//        gotGenesisBlock = false;
//    }
//
//    public int getLength()
//    {
//        return longestChain.size();
//    }
//
//    private void updateLongestChain()
//    {
//        long largestDiff = 0;
//        for (int i = 0; i < difficulties.size(); i++)
//        {
//            if (difficulties.get(i) > largestDiff)
//            {
//                largestDiff = difficulties.get(i);
//                longestChain = chains.get(i);
//            }
//        }
//    }
//
//    public ArrayList<String> getBlockchain()
//    {
//        return longestChain;
//    }
//
//    public Block getBlock(String hash)
//    {
//        return blockMap.get(hash);
//    }
//
//    public Block getLatestBlock()
//    {
//        return blockMap.get(longestChain.get(longestChain.size() - 1));
//    }
//
//    public long getDifficulty()
//    {
//        Block lastBlock = blockMap.get(longestChain.get(longestChain.size() - 1));
//        return lastBlock.getDifficulty();
//    }
//
//    public boolean addBlock(Block block)
//    {
//        // Remove chains with more than 10 block back
//        int largestLength = longestChain.size();
//        for (int i = 0; i < chains.size(); i++)
//        {
//            if (chains.get(i).size() < largestLength - 10)
//            {
//                chains.remove(i);
//                difficulties.remove(i);
//                i--;
//            }
//        }
//
//        if (!block.isGenesis() && !block.validateBlock())
//            return false;
//
//        // Add first block
//        if (!gotGenesisBlock)
//        {
//            gotGenesisBlock = true;
//            chains.add(new ArrayList<String>());
//            chains.get(0).add(block.getHash());
//            difficulties.add(block.getDifficulty());
//            blockMap.put(block.getHash(), block);
//            longestChain = chains.get(0);
//            return true;
//        }
//
//        // Check if there is a duplicate
//        for (int i = 0; i < chains.size(); i++)
//        {
//            String lastBlockHash = chains.get(i).get(chains.get(i).size() - 1);
//            if (lastBlockHash.equals(block.getHash()))
//                return false;
//        }
//
//        // check if block fits into the end of a chain
//        for (int i = 0; i < chains.size(); i++)
//        {
//            String lastBlockHash = chains.get(i).get(chains.get(i).size() - 1);
//            if (lastBlockHash.equals(block.getPreviousHash()))
//            {
//                chains.get(i).add(block.getHash());
//                difficulties.remove(i);
//                difficulties.add(i, block.getDifficulty());
//                blockMap.put(block.getHash(), block);
//                updateLongestChain();
//                return true;
//            }
//        }
//
//        // If it does not fit, then it means there is a fork.
//        // In that case, detect the fork. For detecting the fork,
//        // for each chain, search the last 11 blocks and a new
//        // chain up to the point that the block should be added.
//        for (int i = 0; i < chains.size(); i++)
//        {
//            ArrayList<String> tmpChain = chains.get(i);
//            for (int j = tmpChain.size() - 11; j < tmpChain.size(); j++)
//            {
//                if (j < 0) j = 0;
//
//                if (tmpChain.get(j).equals(block.getPreviousHash()))
//                {
//                    ArrayList<String> newChain = new ArrayList<String>();
//                    for (int k = 0; k <= j; k++)
//                        newChain.add(tmpChain.get(k));
//                    newChain.add(block.getHash());
//                    difficulties.add(block.getDifficulty());
//                    blockMap.put(block.getHash(), block);
//                    updateLongestChain();
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
}
