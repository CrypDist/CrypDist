import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Observable;
import java.util.Observer;

import jdk.nashorn.internal.parser.JSONParser;
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
        if( o instanceof BlockchainManager) {
            JsonObject obj = (JsonObject) arg;
            int flag = obj.get("flag").getAsInt();

            if(flag == 1) {
                client.broadCastMessage(obj.toString());
            } else if (flag == 2) {
                client.broadCastMessage(obj.toString());
             } else
                System.out.println("sa");

        } else if (o instanceof Client) {
            String str = (String) arg;
            JsonObject obj2 = gson.fromJson(str, JsonObject.class);

            int flagValue = obj2.get("flag").getAsInt();


                JsonElement data = obj2.get("data");
                System.out.println("DATA RECEIVED" + data.getAsString());
                blockchainManager.addTransaction(data.getAsString());



        } else
            System.out.println("Err");
    }
}