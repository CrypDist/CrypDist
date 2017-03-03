import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Created by od on 3.03.2017.
 */
public class ReceiveServerRequest implements Runnable {

    private ServerSocket serverSocket;
    private Client client;
    public ReceiveServerRequest(ServerSocket serverSocket, Client client) {
        this.serverSocket = serverSocket;
        this.client = client;
    }

    public void run() {
        System.out.println("Accepting server..");
        while (true) {
            try {
                Socket incomingRequest = serverSocket.accept();

                System.out.println("Accepted:" + incomingRequest.getInetAddress().getCanonicalHostName());
                new Thread(() -> {
                    try {
                        DataInputStream in = new DataInputStream(incomingRequest.getInputStream());
                        int flag = in.readInt();

                        System.out.println("Fflag:" + flag);

                        DataOutputStream out = new DataOutputStream(incomingRequest.getOutputStream());
                        switch (flag){
                            case 1:
                                out.writeInt(0);
                                break;
                            case 2:
                                Peer p = Peer.readObject(new ObjectInputStream(in));
                                out.writeInt(1);
                                client.peerList.add(p);
                                break;
                            case 3:
                                break;
                            case 4:
                                break;
                        }

                        out.flush();

                        incomingRequest.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
            catch (SocketTimeoutException s) {
                System.out.println("Socket timed out!");
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}