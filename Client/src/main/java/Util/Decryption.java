package Util;

import javax.crypto.Cipher;
import java.nio.file.Files;
import java.nio.file.Paths;
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

        String privateKeyContent = new String(Files.readAllBytes(Paths.get("private.pem")));

        privateKeyContent = privateKeyContent.replaceAll("\\n", "").replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "");

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

    public static String decrypt(byte[] secret) {
        try {
            byte[] utf8 = cipher.doFinal(secret);
            return new String(utf8, "UTF8");
        } catch (Exception e ) {
            return "";
        }
    }

}
