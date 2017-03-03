
import java.util.ArrayList;

import static junit.framework.TestCase.assertTrue;

/**
 * Created by Kaan on 17-Feb-17.
 */
public class Main
{
    @org.junit.Test
    public  void main() throws Exception
    {
        BlockchainManager blockchainManager = new BlockchainManager();
        String firstHash = "0x0";
        long timestamp = 0;
        byte[] targetDifficulty = {0,0,0,0};
        Transaction t1 = new Upload("UPLOAD filename 1");
        Transaction t2 = new Upload("UPLOAD filename 2");
        Transaction t3 = new Download("DOWNLOAD 1");
        Transaction t4 = new Download("DOWNLOAD 2");
        ArrayList<Transaction> transactions = new ArrayList<Transaction>();
        transactions.add(t1);
        transactions.add(t2);
        transactions.add(t3);
        transactions.add(t4);

        assertTrue(blockchainManager.getBlockchainLength() == 0);
        Block genesis = new Block(transactions);
        assertTrue(blockchainManager.addBlockToBlockchain(genesis));
        assertTrue(blockchainManager.getBlockchainLength() == 1);

        t1 = new Upload("UPLOAD name 1");
        t2 = new Upload("UPLOAD name 2");
        t3 = new Download("DOWNLOAD 5");
        t4 = new Download("DOWNLOAD 6");
        transactions.removeAll(transactions);
        transactions.add(t1);
        transactions.add(t2);
        transactions.add(t3);
        transactions.add(t4);

        byte[] targetDifficulty2 = {0, 0, 0, 1};
        timestamp = 1;
        long maxNonce = Long.MAX_VALUE;
        String result = blockchainManager.mineBlock(genesis.getHash(), timestamp, maxNonce, transactions);
        String parts[] = result.split(":");
        long nonce = Long.parseLong(parts[1]);
        Block block = new Block(genesis.getHash(), timestamp, nonce, targetDifficulty2, transactions,
                      blockchainManager.getBlockchain());
        blockchainManager.addBlockToBlockchain(block);
        assertTrue(blockchainManager.getBlockchainLength() == 2);
        Block block2 = new Block(genesis.getHash(), timestamp, nonce, targetDifficulty2, transactions,
                blockchainManager.getBlockchain());
        blockchainManager.addBlockToBlockchain(block2);
        assertTrue(blockchainManager.getBlockchainLength() == 2);
    }
}
