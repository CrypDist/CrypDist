import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Created by od on 3.03.2017.
 */
public class ReceiveServerRequest extends Thread {

    private Client client;
    private boolean pending = false;
    public ReceiveServerRequest(Client client) {
        this.client = client;
    }

    public void run() {
        ServerSocket serverSocket;
        Socket server = null;
        while (true) {
            try {

                if(pending){
                    break;
                }

                pending = true;

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

                /* Flag = 400; for hashcodes request, sends the hashcodes to new clients */
                if(flag == 400){
                    ObjectOutputStream pout = new ObjectOutputStream(server.getOutputStream());

                    /* Sending the hash codes to new client */
                    for(String s : client.getBCM().getBlockchain().getBlockMap().keySet())
                    {
                        pout.writeObject(s);
                        pout.flush();
                    }
                }

                /* Flag = 500; for block request with the specified hashcode  */
                if(flag == 500){
                    ObjectOutputStream bout = new ObjectOutputStream(server.getOutputStream());

                        try
                        {
                            /* Send the block with the specified hash code */
                            bout.writeObject(client.getBCM().getBlockchain().getBlock(x));
                            bout.flush();
                        }
                        catch (IOException e)
                        {
                            System.err.println("Block cannot be send to new client");
                        }
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
            pending = false;
        }
    }
}