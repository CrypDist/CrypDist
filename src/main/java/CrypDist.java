import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Observable;
import java.util.Observer;

public class CrypDist implements Observer{

    @Override
    public void update(Observable o, Object arg) {

        Gson gson = new Gson();
        if( o instanceof BlockchainManager) {
            JsonObject obj = (JsonObject) arg;
            int flag = obj.get("flag").getAsInt();

            if(flag == 1) {
                String msg = obj.get("data").toString();
                client.broadCastMessage(msg.getBytes());
            } else if (flag == 2) {
                String msg = obj.get("data").getAsString();
                client.broadCastMessage(msg.getBytes());
            } else
                System.out.println("sa");

        } else if (o instanceof Client) {
            String str = (String) arg;
            JsonObject obj = gson.toJson(str);

        } else
            System.out.println("Err");
    }

    private BlockchainManager blockchainManager;
    private Client client;



    public CrypDist() {

        blockchainManager.addObserver(this);
        client.addObserver(this);

    }






}