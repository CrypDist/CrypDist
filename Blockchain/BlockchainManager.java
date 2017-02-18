package BlockChain;

import java.util.ArrayList;

/**
 * Created by Kaan on 18-Feb-17.
 */
public class BlockchainManager
{
    private Blockchain blockchain;

    public BlockchainManager()
    {
        blockchain = new Blockchain();
    }

    public boolean addBlockToBlockchain(Block block)
    {
        return blockchain.addBlock(block);
    }

    public ArrayList<Block> getBlockchain()
    {
        ArrayList<String> hashChain = blockchain.getBlockchain();
        ArrayList<Block> blocks = new ArrayList<Block>();

        for (int i = 0; i < hashChain.size(); i++)
        {
            blocks.add(blockchain.getBlock(hashChain.get(i)));
        }
        return blocks;
    }
}
