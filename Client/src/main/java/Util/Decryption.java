package Util;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * Created by od on 1.05.2017.
 */
public class Decryption {

    public static Decryption instance;
    public static Cipher cipher;

    public Decryption() throws Exception{

        String privateKeyContent = Config.PRIVATE_KEY.replaceAll("\\n", "").replaceAll("\\r", "").replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "");

        KeyFactory kf = KeyFactory.getInstance("RSA");

        PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent));
        PrivateKey privKey = kf.generatePrivate(keySpecPKCS8);

        cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privKey);
    }

    public static boolean initialization(){
        try {
            instance = new Decryption();
            return true;
        } catch (Exception e ) {
            return false;
        }
    }

    public static String[] decryptGet(byte[] secret) {
        try {
            String result  = new String(cipher.doFinal(secret), "UTF8");
            String[] splitted = result.split(Config.TRANSACTION_KEY_SPLITTER);
            if(splitted.length<2 || splitted.length>2){
                return null;
            }
            return splitted;
        } catch (Exception e ) {
            return null;
        }
    }

}
