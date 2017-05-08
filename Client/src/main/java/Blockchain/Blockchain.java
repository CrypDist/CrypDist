package Blockchain;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Blockchain implements Serializable
{
    private static Logger log = BlockchainManager.log;

    private ConcurrentHashMap<String, Block> blockMap;
    private ArrayList<String> sinkBlocks;
    private Block validBlock;

    public Blockchain(Block genesis)
    {
        blockMap = new ConcurrentHashMap<String, Block>();
        sinkBlocks = new ArrayList<String>();
        validBlock = genesis;
        blockMap.put(genesis.getHash(), genesis);
        sinkBlocks.add(genesis.getHash());
    }

    private void updateConsensus()
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

    public void removeInvalidBlocks(ArrayList<String> keySet)
    {
        for(String key : blockMap.keySet())
        {
            if (!keySet.contains(key) && !key.equals("0x0"))
            {
                boolean points = false;
                for (String keys : blockMap.keySet())
                {
                    String prevHash = blockMap.get(keys).getPreviousHash();
                    if (!prevHash.equals("") && prevHash.equals(key)){
                        points = true;
                        break;
                    }
                }

                String lastHash = getLastBlock();

                while (points && !lastHash.equals(key)){
                    String tempHash = lastHash;
                    lastHash = blockMap.get(lastHash).getPreviousHash();
                    blockMap.remove(tempHash);
                }
                if (blockMap.get(lastHash).getPreviousHash().equals("")){
                    validBlock = blockMap.get(lastHash);
                    sinkBlocks.clear();
                    sinkBlocks.add(validBlock.getHash());
                }
                else {
                    validBlock = blockMap.get(blockMap.get(lastHash).getPreviousHash());
                    sinkBlocks.clear();
                    sinkBlocks.add(validBlock.getHash());

                    blockMap.remove(key);
                }

            }
        }
    }

    private void removeOldBlocks()
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
