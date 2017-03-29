
import java.util.ArrayList;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by Kaan on 17-Feb-17.
 */
public class BlockchainTest
{
    @org.junit.Test
    public  void main() throws Exception
    {
        Block genesis = new Block();
        BlockchainManager blockchainManager = new BlockchainManager(genesis);
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

        long timestamp = 1;
        long maxNonce = Long.MAX_VALUE;
        long nonce = blockchainManager.mineBlock(genesis.getHash(), timestamp, maxNonce, transactions);
        Block block = new Block(genesis.getHash(), timestamp, nonce, transactions, blockchainManager.getBlockchain());
        blockchainManager.addBlockToBlockchain(block);
        assertTrue(blockchainManager.getBlockchainLength() == 1);
        Block block2 = new Block(genesis.getHash(), timestamp, nonce, transactions, blockchainManager.getBlockchain());
        blockchainManager.addBlockToBlockchain(block2);
        assertTrue(blockchainManager.getBlockchainLength() == 1);
    }
}
