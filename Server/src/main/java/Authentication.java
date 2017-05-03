import javax.crypto.Cipher;
import java.util.HashMap;

/**
 * Created by od on 30.04.2017.
 */
public class Authentication {
    private static Authentication instance;
    private static HashMap<String,String> list;

    public static void initalization() {
        list = new HashMap<>();
        instance = new Authentication();
    }


    public static boolean Authenticate(String id, String pass) {
        String result = list.get(id);
        if(result.equals(pass)) {
            return true;
        }
        else {
            return false;
        }
    }


    public Authentication() {
        list.put("Client1","Pass1");
        list.put("Client2","Pass2");
        list.put("Client3","Pass3");
        list.put("Client4","Pass4");
        list.put("Client5","Pass5");
        list.put("Client6","Pass6");
        list.put("Client7","Pass7");
        list.put("Client8","Pass8");
        list.put("Client9","Pass9");
        list.put("Client10","Pass10");
        list.put("Client11","Pass11");
        list.put("Client12","Pass12");
        list.put("Client13","Pass13");

    }

}
