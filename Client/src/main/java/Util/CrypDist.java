package Util;

import Blockchain.Block;
import Blockchain.BlockchainManager;
import P2P.Client;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.sf.json.JSON;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

public class CrypDist {

    private static transient  Logger log = Logger.getLogger("CrypDist");


    // Flag 1 = Transaction data
    // Flag 2 = Hash
    // Flag 3 = Valid transaction message
    // Flag 4 = Validate my blockchain (taken from blockchainManager)

    public BlockchainManager blockchainManager;
    Client client;

    public CrypDist(String swAdr, int swPort, int hbPort, int serverPort ) {

        blockchainManager = new BlockchainManager(this);
        client = new Client(swAdr, swPort, hbPort,serverPort, this);

        Thread t = new Thread(client);
        t.start();
        updateBlockchain();
    }

    public String updateByClient(String arg) {

        log.info("BE NOTIFIED");
        Gson gson = new Gson();
        String lastHash = blockchainManager.getBlockchain().getLastBlock();

        String strToBeSplitted = (String) arg;
        String[] elems = strToBeSplitted.split("////");
        String ip = elems[0];
        String str = elems[1];

        if(ip.equals("X")) {
            log.info("Pair size is now " + str);
            blockchainManager.setNumOfPairs(Integer.parseInt(str));
            return "";
        }

        log.info("DATA IS" + str);

        JsonObject obj2 = gson.fromJson(str, JsonObject.class);

        int flagValue = obj2.get("flag").getAsInt();

        if(flagValue == Config.MESSAGE_REQUEST_KEYSET) {
            return blockchainManager.getKeySet();
        }

        if(flagValue == Config.MESSAGE_REQUEST_BLOCK) {
            log.info("BLOCK REQUESTED.");
            JsonElement data = obj2.get("data");
            String dataStr = data.getAsString();

            return gson.toJson(blockchainManager.getBlock(dataStr),Block.class);
        }

        String hashValue = obj2.get("lastHash").getAsString();
        if (blockchainManager.validateHash(hashValue)) {
            if (flagValue == 1) {
                JsonObject toReturn = new JsonObject();
                JsonElement data = obj2.get("data");
                String dataStr = data.getAsString();

                toReturn.addProperty("transaction", dataStr);
                toReturn.addProperty("flag", 3);
                toReturn.addProperty("lastHash", hashValue);
                client.sendMessage(ip, gson.toJson(toReturn));

                log.info("DATA RECEIVED" + dataStr);

                blockchainManager.addTransaction(dataStr);
            }
            else if (flagValue == 2) {
                JsonElement data = obj2.get("data");
                JsonElement time = obj2.get("timeStamp");
                JsonElement blockId = obj2.get("blockId");

                log.info("HASH RECEIVED" + data.getAsString());
                blockchainManager.receiveHash(data.getAsString(), time.getAsLong(), blockId.getAsString());
            }
            else if (flagValue == 3)
            {
                JsonElement transaction = obj2.get("transaction");
                blockchainManager.markValid(transaction.getAsString());
            }

        }
        return "";
    }

    public String updateByBlockchain(Object arg) {
        log.info("BE NOTIFIED");
        Gson gson = new Gson();
        String lastHash = blockchainManager.getBlockchain().getLastBlock();

        JsonObject obj = (JsonObject) arg;
        obj.addProperty("lastHash", lastHash);
        int flag = obj.get("flag").getAsInt();

        if(flag == 1) {
            client.broadCastMessage(obj.toString());
        }
        else if (flag == 2) {
            log.info("HASH BROADCAST IS IN PROCESS");
            client.broadCastMessage(obj.toString());
        }
        else if (flag == 4) {
            updateBlockchain();
            log.warn("HASH IS NOT UP TO DATE");
        }
        else
            log.error("Invalid flag");

        return "";
    }

    public void updateBlockchain()
    {
        // UPDATE BLOCKCHAIN
        HashSet<String> keySet = client.receiveKeySet();
        if(keySet.size() == 0)
            return;
        Set<String> neededBlocks = blockchainManager.getNeededBlocks(keySet);
        if(neededBlocks.size() == 0)
            return;

        HashMap<String, String> blocks = client.receiveBlocks(neededBlocks);
        for(String s : blocks.values())
            log.info("BLOOCKS: " + s);

        blockchainManager.addNewBlocks(blocks);
    }
}