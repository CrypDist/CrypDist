package Util;

import org.apache.log4j.Logger;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * Created by od on 1.05.2017.
 */
public class Decryption {
    private static transient Logger log = Logger.getLogger("Decryption");
    public static Decryption instance;
    public static Cipher cipher;
    public static final Object lock = new Object();

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
            log.debug(e);
            return false;
        }
    }

    public static String[] decryptGet(byte[] secret) {
        synchronized (lock) {
            try {
                String result = new String(cipher.doFinal(secret), "UTF8");
                log.debug(result);
                String[] splitted = result.split(Config.KEY_SPLITTER);
                if (splitted.length < 2 || splitted.length > 2) {
                    log.warn("SPLITTED SIZE=\t" + splitted.length);
                    for (String str : splitted)
                        log.warn("SPLITTED\t" + str);
                    return null;
                }
                return splitted;
            } catch (Exception e) {
                log.debug(e);
                return null;
            }
        }
    }

}
