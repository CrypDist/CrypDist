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
public class ReceiveServerRequest extends Thread {

    private Client client;
    public ReceiveServerRequest(Client client) {
        this.client = client;
    }

    public void run() {
        ServerSocket serverSocket;
        Socket server = null;
        while (true) {
            try {
                serverSocket = new ServerSocket(client.getServerPort());
                server = serverSocket.accept();
                serverSocket.close();

                System.out.println("Server request incoming.");

                ObjectInputStream in = new ObjectInputStream(new DataInputStream(server.getInputStream()));
                int flag = in.readInt();
                String x = (String) in.readObject();

                if(flag == 200) {
                    System.out.print("The received messsage:" +  x + "\n");
                }
                if(flag == 300) {
                    client.broadCastMessage(x);
                }

                server.close();
            }
            catch (SocketTimeoutException s) {
                System.err.println("Server socket timed out!");
            } catch (IOException e) {
                System.err.println("IOException while receiving server request!");
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                System.err.println("Error read.");
            }finally {
                if(server != null && !server.isClosed())
                    try {
                        server.close();
                    } catch (Exception e) {
                        continue;
                    }

            }
        }
    }
}