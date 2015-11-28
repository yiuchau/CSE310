import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Working on dealing with concurrency
 */
public class Server {

    public static final HashMap<Record, String> records = new HashMap<>();

    public static class TCPSocket extends Thread {
        private Socket socket;

        public TCPSocket(Socket socket) {
            this.socket = socket;
        }

        public void run() {

            try {
                //create an input stream from the socket input stream
                BufferedReader inFromClient = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));


                // create an output stream from the socket output stream
                DataOutputStream outToClient =
                        new DataOutputStream(socket.getOutputStream());

                String clientSentence, serverResponse;

                while ((clientSentence = inFromClient.readLine()) != null) {

                    System.out.println("Received: " + clientSentence);

                    String [] tokens = clientSentence.split("[ ]+");

                    // Process command according to protocol
                    switch (tokens[0]){
                        case "EXIT":
                            System.out.println("Closing connection socket for client at Address:Port " +
                                    socket.getRemoteSocketAddress().toString());

                            socket.close();
                            break;
                        case "PUT":
                        /*
                            put:
                            a. Three args: name, value, and type
                            b. Add name records to database, sent to server
                            c. Server records the new entry for name/type, or updates old entry
                        */

                            synchronized (records) {
                                System.out.println("Retrieving records");

                                Record record = new Record(tokens[1], Record.Type.valueOf(tokens[2]));

                                records.put(record, tokens[3]);
                            }

                            outToClient.writeBytes("Record added\n");


                            break;
                        case "GET":
                        /*
                            get:
                            a. Queries server database
                            b. Two arguments: name , type
                            c. Server returns record value if found
                            d. Returns not found error message if not found
                        */
                            break;

                        case "BROWSE":
                        /*  browse:
                            a. No args
                            b. Retrieve all current name records in database
                            c. Returns all name and type fields of all records
                            d. If database is empty, return a database is empty error
                        */

                            synchronized (records) {
                                StringBuilder output = new StringBuilder();
                                for (Record record : records.keySet()) {
                                    output.append(record.getName());
                                    output.append(" ");
                                    output.append(record.getType());
                                    output.append(" ");
                                    output.append(records.get(record));
                                    output.append("\n");
                                }

                                System.out.println(output);

                                outToClient.writeBytes(output.toString());
                            }

                            break;

                        case "del":
                        /*  del:
                            a. Server removes a name record from database
                            b. Two arguments: name, type
                            c. If found, removes record and sends positive feedback
                            d. Returns not found error message otherwise
                        */
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] argv) throws Exception {


        // create a server socket (TCP)
        ServerSocket welcomeSocket = new ServerSocket(10000);
        System.out.println("Server is at port: " + welcomeSocket.getLocalPort());

        // loop infinitely (process clients sequentially)
        while (true) {
            // Wait and accept client connection
            Socket connectionSocket = welcomeSocket.accept();

            new TCPSocket(connectionSocket).start();
        }
    }
}
