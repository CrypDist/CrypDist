package Util;

import Blockchain.BlockchainManager;
import P2P.Client;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Set;

public class CrypDist {

    private static transient  Logger log = Logger.getLogger("CrypDist");
    private byte[] sessionKey;
    private boolean authenticated;
    // Flag 1 = Transaction data
    // Flag 2 = Hash
    // Flag 3 = Valid transaction message
    // Flag 4 = Validate my blockchain (taken from blockchainManager)

    public BlockchainManager blockchainManager;
    Client client;

    public CrypDist() {
        if(!Decryption.initialization())
            log.info("Decryption service cannot be created.");

        client = new Client(this);
        blockchainManager = new BlockchainManager(this, sessionKey);
        Thread t = new Thread(client);
        t.start();
        blockchainManager.buildBlockchain();
        updateBlockchain();
    }

    public String updateByClient(String arg) {

        Gson gson = new Gson();
        String lastHash = blockchainManager.getBlockchain().getLastBlock();

        String strToBeSplitted = arg;
        String[] elems = strToBeSplitted.split(Config.CLIENT_MESSAGE_SPLITTER);
        String ip = elems[0];
        String str = elems[1];

        if(ip.equals(Config.CLIENT_MESSAGE_PEERSIZE)) {
            log.info("Pair size is now " + str);
            blockchainManager.setNumOfPairs(Integer.parseInt(str));
            return "";
        }

        JsonObject obj2 = gson.fromJson(str, JsonObject.class);

        int flagValue = obj2.get("flag").getAsInt();

        if(flagValue == Config.MESSAGE_REQUEST_KEYSET) {
            return blockchainManager.getKeySet();
        }

        if(flagValue == Config.MESSAGE_REQUEST_BLOCK) {
            log.info("BLOCK REQUESTED.");
            JsonElement data = obj2.get("data");
            String dataStr = data.getAsString();

            return gson.toJson(blockchainManager.getBlock(dataStr));
        }

        String hashValue = obj2.get("lastHash").getAsString();
        byte[] key = Base64.getDecoder().decode(obj2.get("key").getAsString());
        String[] credentials = Decryption.decryptGet(key);
        if(credentials == null){
            log.error("The incoming message includes false key");
            return "";
        }


        String messageIp = credentials[0];
        String username = credentials[1];

        if (blockchainManager.validateHash(hashValue) && ip.equals(messageIp)) {
            if (flagValue == Config.FLAG_BROADCAST_TRANSACTION) {
                JsonObject toReturn = new JsonObject();
                JsonElement data = obj2.get("data");
                String dataStr = data.getAsString();

                toReturn.addProperty("transaction", dataStr);
                toReturn.addProperty("flag", Config.FLAG_TRANSACTION_VALIDATION);
                toReturn.addProperty("lastHash", hashValue);
                toReturn.addProperty("key", Base64.getEncoder().encodeToString(sessionKey));
                client.sendMessage(ip, gson.toJson(toReturn));

                //log.info("DATA RECEIVED" + dataStr);

                blockchainManager.addTransaction(dataStr,ip);
            }
            else if (flagValue == Config.FLAG_BROADCAST_HASH) {
                JsonElement data = obj2.get("data");
                JsonElement time = obj2.get("timeStamp");
                JsonElement blockId = obj2.get("blockId");

                blockchainManager.receiveHash(data.getAsString(), time.getAsLong(), blockId.getAsString());
            }
            else if (flagValue == Config.FLAG_TRANSACTION_VALIDATION)
            {
                JsonElement transaction = obj2.get("transaction");

                if (username.length() > 2)
                    blockchainManager.markValid(transaction.getAsString(),username);
            }
        } else {
            log.error("Incoming message has wrong key or invalid hash.");
            return "";
        }
    }

    public String updateByBlockchain(Object arg) {
        log.info("BE NOTIFIED");
        Gson gson = new Gson();
        String lastHash = blockchainManager.getBlockchain().getLastBlock();

        JsonObject obj = (JsonObject) arg;
        obj.addProperty("lastHash", lastHash);
        obj.addProperty("key", Base64.getEncoder().encodeToString(sessionKey));

        log.error("Key " + sessionKey);
        log.error("Key byte " + Base64.getEncoder().encodeToString(sessionKey));
        int flag = obj.get("flag").getAsInt();

        if(flag == Config.FLAG_BROADCAST_TRANSACTION) {
            client.broadCastMessage(obj.toString());
        }
        else if (flag == Config.FLAG_BROADCAST_HASH) {
            log.info("HASH BROADCAST IS IN PROCESS");
            client.broadCastMessage(obj.toString());
        }
        else if (flag == Config.FLAG_BLOCKCHAIN_INVALID) {
            log.warn("HASH UPDATE IS IN PROGRESS");
            updateBlockchain();
        }
        else
            log.error("Invalid flag");

        return "";
    }

    public void updateBlockchain()
    {
        synchronized (this) {
            // UPDATE BLOCKCHAIN
            log.warn("Blockchain is not updated, start the procedure!");
            ArrayList<String> keySet = client.receiveKeySet();
            if (keySet.size() == 0)
                return;
            Set<String> neededBlocks = blockchainManager.getNeededBlocks(keySet);
            if (neededBlocks.size() == 0)
                return;

            HashMap<String, String> blocks = client.receiveBlocks(neededBlocks);

            for (String str : blocks.values())
                log.info("BLOCK " + str);

            blockchainManager.removeInvalidBlocks(keySet);
            blockchainManager.addNewBlocks(blocks);
            blockchainManager.setUpdated();
        }
    }

    public void setSessionKey(byte[] sessionKey) {
        this.sessionKey = sessionKey;
    }

    public byte[] getSessionKey() {
        return sessionKey;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }
}