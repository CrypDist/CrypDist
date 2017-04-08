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

                System.out.println("Type of message: " + flag);
                System.out.print("The received messsage:");
                String x = (String) in.readObject();
                System.out.println(x);
                System.out.println();

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