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

public class CrypDist implements Observer{

    private static transient  Logger log = Logger.getLogger("Util.CrypDist");

    // Flag 1 = Transaction data
    // Flag 2 = Hash
    // Flag 3 = Valid transaction message
    // Flag 4 = Validate my blockchain (taken from blockchainManager)

    public BlockchainManager blockchainManager;
    Client client;

    public CrypDist(String swAdr, int swPort, int hbPort, int serverPort ) {

        PropertyConfigurator.configure(getClass().getResourceAsStream("log4j_custom.properties"));

        blockchainManager = new BlockchainManager(this);
        client = new Client(swAdr, swPort, hbPort,serverPort, this);
        Thread t = new Thread(client);
        blockchainManager.addObserver(this);
        client.addObserver(this);
        t.start();
    }

    @Override
    public void update(Observable o, Object arg) {

        log.info("BE NOTIFIED");
        Gson gson = new Gson();
        String lastHash = blockchainManager.getBlockchain().getLastBlock();
        if( o instanceof BlockchainManager) {
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

        }
        else if (o instanceof Client) {
            String strToBeSplitted = (String) arg;
            String[] elems = strToBeSplitted.split("////");
            String ip = elems[0];
            String str = elems[1];

            if(ip.equals("X")) {
                log.info("Pair size is now " + str);
                blockchainManager.setNumOfPairs(Integer.parseInt(str));
                return;
            }

            JsonObject obj2 = gson.fromJson(str, JsonObject.class);

            int flagValue = obj2.get("flag").getAsInt();
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
        }
        else
            log.error("Unknown observable");
    }

    public HashMap<String, JsonObject> updateBlockchain()
    {
        // UPDATE BLOCKCHAIN
        HashSet<String> keySet = client.receiveKeySet();
        Set<String> neededBlocks = blockchainManager.getNeededBlocks(keySet);
        HashMap<String, JsonObject> blocks = client.receiveBlocks(neededBlocks);
        return blocks;
    }
}