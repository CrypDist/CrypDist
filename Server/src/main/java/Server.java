import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server implementation for establishing connection among peers.
 *
 * The list of clients are held in server to be sent whenever a new client is connected.
 *
 * Periodically, server sends heartbeats to clients to check whether they are alive or not. The clients with no response
 *  are deleted from list.
 *
 * Created by od on 16.02.2017.
 */
public class Server extends Thread {

    ConcurrentHashMap<Peer,Integer> peerList;
    private final static Object lock = new Object();
    private ServerSocket serverSocket;

    private Cipher cipher;

    public Server(int port) throws IOException {

        String publicKeyContent = new String(Files.readAllBytes(Paths.get("public.pem")));

        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent));
            RSAPublicKey pubKey = (RSAPublicKey) kf.generatePublic(keySpecX509);
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE,pubKey);
        } catch (Exception e){
            throw new IOException("Something is wrong with Encryption.");
        }



        peerList = new ConcurrentHashMap<>();

        //Opening serverSocket
        serverSocket = new ServerSocket(port);

    }

    public void run() {

        //Timer action is used for periodical heartbeats to clients.
        Timer timer = new Timer();
        timer.schedule(new HeartBeatTask(peerList), 1000, 5 * 1000);

        //Server constantly accepts for new client connections.
        //Recall that serverSocket.accept() is a blocking call.
        while (true) {
            try {
                Socket newConnection = serverSocket.accept();

                new Thread(() -> {
                    try {
                        Peer p = new Peer(newConnection.getInetAddress(),1,1);
                        DataOutputStream out = new DataOutputStream(newConnection.getOutputStream());

                        synchronized (lock) {
                            //Whenever a new client is connected, the number of alive clients and their objects are sent.
                            int x = peerList.size();

                            if (x != 0) {
                                if (peerList.containsKey(p))
                                    out.writeInt(x - 1);
                                else
                                    out.writeInt(x);
                            } else
                                out.writeInt(x);

                            out.flush();

                            for (Peer peer : peerList.keySet()) {
                                if (!p.equals(peer))
                                    peer.writeObject(new ObjectOutputStream(out));
                            }
                            out.flush();

                            //The data for dataPort and heartBeatPort for newly connected client is taken from same socket.
                            DataInputStream in = new DataInputStream(newConnection.getInputStream());
                            int port = in.readInt();
                            int port2 = in.readInt();
                            String id = in.readUTF();
                            String pass = in.readUTF();

                            boolean valid = Authentication.Authenticate(id,pass);

                            if (valid) {
                                byte [] msg = generateKey(newConnection.getInetAddress().toString(),id);
                                out.writeInt(msg.length);
                                out.write(msg);
                                out.flush();
                            }
                            p = new Peer(newConnection.getInetAddress(),port,port2 );

                            peerList.put(p,0);
                        }

                        System.out.println(newConnection.getInetAddress() + " is connected.");


                        newConnection.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }).start();


            }
            catch (SocketTimeoutException s) {
                System.out.println("Socket timed out!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public byte[] generateKey(String ip, String username) {
        try {
            String s = ip + username;
            return cipher.doFinal(s.getBytes());
        } catch (Exception e) {
            return "".getBytes();
        }
    }


}
