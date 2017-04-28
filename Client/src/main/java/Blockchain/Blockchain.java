package Blockchain;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Blockchain implements Serializable
{
    private static Logger log = BlockchainManager.log;

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
                log.warn("LAST HASH IS NOW: " + validBlock.getHash());
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
            if ((block.getHash()).equals(tmpBlock.getHash()))
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

    public Set<String> getNeededBlocks(Set<String> keySet)
    {

        log.info("KEYSET IS:");

        for(String s : keySet) {
            log.info("ITEM " + s);
        }

        log.info("BLOCKMAP IS:");

        for(String s : blockMap.keySet()) {
            log.info("MAP " + s);
        }

        Set<String> neededBlocks = new HashSet<>();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext())
        {
            String key = iterator.next();
            if (!blockMap.containsKey(key)) {
                neededBlocks.add(key);
                log.info(key + " is added!!!!!!!!!!!!!!!!!");
            }
        }
        return neededBlocks;
    }

    public Set<String> getKeySet() {
        return new HashSet<String>(blockMap.keySet());
    }
}
