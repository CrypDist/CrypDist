package Util;

import Blockchain.BlockchainManager;
import P2P.Client;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.log4j.Logger;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
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
        byte[] dummy = new byte[1];

        String hashValue = obj2.get("lastHash").getAsString();
        byte[] key = Base64.getDecoder().decode( gson.fromJson(obj2.get("key").getAsString(),dummy.getClass()));
        String[] credentials = Decryption.decryptGet(key);
        if(credentials == null){
            log.error("The incoming message includes false key");
            return "";
        }


        String messageIp = credentials[0];
        String username = credentials[1];

        JsonObject toReturn = new JsonObject();
        toReturn.addProperty("key", Base64.getEncoder().encodeToString(sessionKey));

        if(ip.equals(messageIp)) {
            if (blockchainManager.validateHash(hashValue)) {

                if (flagValue == Config.FLAG_BROADCAST_TRANSACTION) {

                    JsonElement data = obj2.get("data");
                    String dataStr = data.getAsString();

                    toReturn.addProperty("response", Config.MESSAGE_RESPONSE_VALID);
                    toReturn.addProperty("transaction", dataStr);
                    toReturn.addProperty("lastHash", hashValue);

                    blockchainManager.addTransaction(dataStr, ip);

                } else if (flagValue == Config.FLAG_BROADCAST_HASH) {
                    toReturn.addProperty("response", Config.MESSAGE_RESPONSE_VALID);

                    JsonElement data = obj2.get("data");
                    JsonElement time = obj2.get("timeStamp");
                    JsonElement blockId = obj2.get("blockId");
                    blockchainManager.receiveHash(data.getAsString(), time.getAsLong(), blockId.getAsString());
                }

            } else {
                toReturn.addProperty("response", Config.MESSAGE_RESPONSE_INVALIDHASH);
                log.error("Incoming message has invalid hash.");
                return "";
            }
        }
        else  {
            toReturn.addProperty("response", Config.MESSAGE_RESPONSE_INVALIDKEY);
        }
        return gson.toJson(toReturn);
    }

    public String updateByBlockchain(Object arg) {
        log.info("BE NOTIFIED");
        Gson gson = new Gson();
        String lastHash = blockchainManager.getBlockchain().getLastBlock();

        JsonObject obj = (JsonObject) arg;
        obj.addProperty("lastHash", lastHash);
        byte [] keyStr = Base64.getEncoder().encode(sessionKey);
        obj.addProperty("key", gson.toJson(keyStr));

        int flag = obj.get("flag").getAsInt();

        if(flag == Config.FLAG_BROADCAST_TRANSACTION) {
            HashMap<String,String> results = client.broadCastMessageResponseWithIp(obj.toString());

            int totalValidResponses = 0;
            int totalValidations = 0;
            int totalInvalidKeysResponses = 0;
            int totalInvalidHashResponses = 0;
            String transaction = null;

            for(Map.Entry<String,String> entry : results.entrySet()) {

                JsonObject result = gson.fromJson(entry.getValue(), JsonObject.class);
                byte[] key = Base64.getDecoder().decode(result.get("key").getAsString());
                String[] credentials = Decryption.decryptGet(key);
                if (credentials == null) {
                    continue;
                }
                String messageIp = credentials[0];
                String username = credentials[1];


                if (messageIp.equals(entry.getKey()) && username.length() > 2) {
                    int response = result.get("response").getAsInt();

                    if (response == Config.MESSAGE_RESPONSE_INVALIDKEY) {
                        totalInvalidKeysResponses++;
                    }
                    if (response == Config.MESSAGE_RESPONSE_VALID) {

                        if(transaction != null && !transaction.equals(result.get("transaction").getAsString()))
                            log.error("WRONG RESPONSE");

                        transaction = result.get("transaction").getAsString();
                        totalValidations++;
                    }
                    if (response == Config.MESSAGE_RESPONSE_INVALIDHASH)
                        totalInvalidHashResponses++;

                    totalValidResponses++;
                }
            }

            if(totalValidations > totalValidResponses/2+1)
                blockchainManager.markValid(transaction);
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