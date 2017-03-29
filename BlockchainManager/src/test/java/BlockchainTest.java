
import java.util.ArrayList;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by Kaan on 17-Feb-17.
 */
public class BlockchainTest
{
    private static BlockchainManager blockchainManager = new BlockchainManager(new Block());
    Transaction t1 = new Upload("UPLOAD filename 1");
    Transaction t2 = new Upload("UPLOAD filename 2");
    Transaction t3 = new Download("DOWNLOAD 1");
    Transaction t4 = new Download("DOWNLOAD 2");
    ArrayList<Transaction> transactions = new ArrayList<Transaction>();
    private static Block genesis = new Block();

    public BlockchainTest()
    {
        transactions.add(t1);
        transactions.add(t2);
        transactions.add(t3);
        transactions.add(t4);
    }

    @org.junit.BeforeClass
    public static void setupClass()
    {
        blockchainManager = new BlockchainManager(genesis);
        assertNotEquals(null, blockchainManager);
        assertTrue(blockchainManager.getBlockchainLength() == 0);
    }

    @org.junit.Test
    public void setup()
    {
        assertNotEquals(null, blockchainManager.getBlockchain());
        assertNotEquals(null, blockchainManager.getBlockchain().getBlock(genesis.getHash()));
    }

    @org.junit.Test
    public void addBlocks() throws Exception
    {
        long timestamp;
        long maxNonce = Long.MAX_VALUE;
        long nonce;
        Block prev = genesis;
        Block third = null;

        for (int i = 0; i < 6; i++)
        {
            timestamp = i + 1;
            nonce = blockchainManager.mineBlock(prev.getHash(), timestamp, maxNonce, transactions);
            Block block = new Block(prev.getHash(), timestamp, nonce, transactions, blockchainManager.getBlockchain());
            String hash = block.getHash();
            assertTrue(hash.substring(0, 2).equals("00"));
            blockchainManager.addBlockToBlockchain(block);
            assertTrue(blockchainManager.getBlockchainLength() == i + 1);
            prev = block;

            if (i == 2)
                third = block;
        }

        timestamp = 10;
        nonce = blockchainManager.mineBlock(third.getHash(), timestamp, maxNonce, transactions);
        Block block4 = new Block(third.getHash(), timestamp, nonce, transactions, blockchainManager.getBlockchain());
        blockchainManager.addBlockToBlockchain(block4);
        assertTrue(blockchainManager.getBlockchainLength() == 6);

        timestamp = 11;
        nonce = blockchainManager.mineBlock(block4.getHash(), timestamp, maxNonce, transactions);
        Block block5 = new Block(block4.getHash(), timestamp, nonce, transactions, blockchainManager.getBlockchain());
        blockchainManager.addBlockToBlockchain(block5);
        assertTrue(blockchainManager.getBlockchainLength() == 6);

        for (int i = 6; i < 9; i++)
        {
            timestamp = i + 1;
            nonce = blockchainManager.mineBlock(prev.getHash(), timestamp, maxNonce, transactions);
            Block block = new Block(prev.getHash(), timestamp, nonce, transactions, blockchainManager.getBlockchain());
            String hash = block.getHash();
            assertTrue(hash.substring(0, 2).equals("00"));
            blockchainManager.addBlockToBlockchain(block);
            assertTrue(blockchainManager.getBlockchainLength() == i + 1);
            prev = block;
        }

        assertNotEquals(null, blockchainManager.getBlockchain().getBlock(third.getHash()));
        assertNotEquals(null, blockchainManager.getBlockchain().getBlock(block4.getHash()));
        assertNotEquals(null, blockchainManager.getBlockchain().getBlock(block5.getHash()));

        timestamp = 90;
        nonce = blockchainManager.mineBlock(prev.getHash(), timestamp, maxNonce, transactions);
        Block block10 = new Block(prev.getHash(), timestamp, nonce, transactions, blockchainManager.getBlockchain());
        blockchainManager.addBlockToBlockchain(block10);
        assertTrue(blockchainManager.getBlockchainLength() == 10);
        assertNotEquals(null, blockchainManager.getBlockchain().getBlock(third.getHash()));
        assertEquals(null, blockchainManager.getBlockchain().getBlock(block4.getHash()));
        assertEquals(null, blockchainManager.getBlockchain().getBlock(block5.getHash()));
    }
}
