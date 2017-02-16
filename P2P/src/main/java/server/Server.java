package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.HashSet;

/**
 * Created by od on 16.02.2017.
 */
public class Server extends Thread {

    HashSet<SocketAddress> peerList;
    private ServerSocket serverSocket;

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        peerList = new HashSet<SocketAddress>();
    }

    private void sendPeerList(Socket socket) throws IOException {
        //Serialize and send the socket.
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
    }


    private void refreshList() {
        //Send heartbeats to each peer and remove the ones doesnt responding
    }

    public void run() {
        while (true) {
            try {
                Socket server = serverSocket.accept();

                System.out.println("Connected to " + server.getRemoteSocketAddress());
                peerList.add(server.getRemoteSocketAddress());
                System.out.println(server.getInetAddress());

                //DataInputStream in = new DataInputStream(server.getInputStream());


            } catch (SocketTimeoutException s) {
                System.out.println("Socket timed out!");
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
