import Blockchain.BlockchainManager;
import P2P.Client;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Observable;
import java.util.Observer;

public class CrypDist implements Observer{


    BlockchainManager blockchainManager;
    Client client;

    public CrypDist(String swAdr, int swPort, int hbPort, int serverPort ) {

        blockchainManager = new BlockchainManager();
        client = new Client(swAdr, swPort, hbPort,serverPort );
        Thread t = new Thread(client);
        blockchainManager.addObserver(this);
        client.addObserver(this);
        t.start();
    }
    @Override
    public void update(Observable o, Object arg) {

        System.out.println("BE NOTIFIED");
        Gson gson = new Gson();
        String lastHash = blockchainManager.getBlockchain().getLastBlock();
        if( o instanceof BlockchainManager) {
            JsonObject obj = (JsonObject) arg;
            obj.addProperty("lastHash", lastHash);
            int flag = obj.get("flag").getAsInt();

            if(flag == 1) {
                client.broadCastMessage(obj.toString());
            } else if (flag == 2) {
                System.out.println("HASH BROADCAST IS IN PROCESS");
                client.broadCastMessage(obj.toString());
             } else
                System.out.println("sa");

        } else if (o instanceof Client) {
            String str = (String) arg;
            JsonObject obj2 = gson.fromJson(str, JsonObject.class);

            int flagValue = obj2.get("flag").getAsInt();
            String hashValue = obj2.get("lastHash").getAsString();
            if (blockchainManager.validateHash(hashValue)) {

                if (flagValue == 1) {
                    JsonElement data = obj2.get("data");
                    System.out.println("DATA RECEIVED" + data.getAsString());
                    blockchainManager.addTransaction(data.getAsString());
                } else if (flagValue == 2) {
                    JsonElement data = obj2.get("data");
                    JsonElement time = obj2.get("timeStamp");
                    JsonElement blockId = obj2.get("blockId");
                    System.out.println("HASH RECEIVED" + data.getAsString());
                    blockchainManager.receiveHash(data.getAsString(), time.getAsLong(), blockId.getAsString());
                }
            }
            else
            {
                // TODO REPLY WITH UPDATE YOUR BLOCKCHAIN
            }

        } else
            System.out.println("Err");
    }
}