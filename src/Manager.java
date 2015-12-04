
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 */
public class Manager {

    public static final HashMap<String, Server> servers = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static void main(String[] argv) throws Exception {

        BufferedReader in = new BufferedReader(new FileReader(new File("manager.in")));

        String type;

        // add one server for each type
        while ((type = in.readLine()) != null) {
            servers.put(type, new Server());
        }

        // create a server socket (TCP)
        ServerSocket welcomeSocket = new ServerSocket(0);
        System.out.println("Manager server is at port: " + welcomeSocket.getLocalPort());

        String line = null;

        // loop infinitely (process clients sequentially)
        while (true) {
            // Wait and accept client connection
            Socket connectionSocket = welcomeSocket.accept();

            //create an input stream from the socket input stream
            BufferedReader inFromClient = new BufferedReader(
                    new InputStreamReader(connectionSocket.getInputStream()));


            // create an output stream from the socket output stream
            DataOutputStream outToClient =
                    new DataOutputStream(connectionSocket.getOutputStream());

            type =  inFromClient.readLine().split(":\\s+")[1];

            Server requestedServer = servers.get(type);

        }
    }
}
