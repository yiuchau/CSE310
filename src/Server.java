import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Server
 *
 * Request messages:
 * Method: PUT/GET/BROWSE/DEL/EXIT
 * Name: test.com
 * Type: A/NS
 * Value: 192.168.1.1
 *
 * Response message:
 * Status: OK/FAIL
 * Response: 192.168.1.1 (value of record)
 *
 */
public class Server {

    public static final HashMap<String, String> records = new HashMap<>();

    /**
     * Get the value from a String formatted as Key: Value
     * @param line line to get the value from
     * @return the value
     */
    private static String getValue(String line) {
        return line.split("\\s+")[1];
    }

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

                String methodName, serverResponse;

                boolean closed = false;

                String[] tokens = {"","","",""};

                while ((methodName = inFromClient.readLine()) != null) {

                    System.out.println("Received: " + methodName);

                    methodName = getValue(methodName);

                    // Process command according to protocol
                    switch (methodName){
                        case "EXIT":
                            System.out.println("Closing connection socket for client at Address:Port " +
                                    socket.getRemoteSocketAddress().toString());

                            socket.close();
                            closed = true;
                            break;
                        case "PUT":
                        /*
                            put:
                            a. Three args: name, value, and type
                            b. Add name records to database, sent to server
                            c. Server records the new entry for name/type, or updates old entry
                        */

                            String name = getValue(inFromClient.readLine());
                            String type = getValue(inFromClient.readLine());
                            String value = getValue(inFromClient.readLine());

                            if (!type.equals("NS") && !type.equals("A")) {
                                outToClient.writeBytes("Invalid record type\n");
                                break;
                            }

                            synchronized (records) {
                                System.out.println("Adding or updating record");

                                records.put(name + " " + type, value);
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

                            synchronized (records) {
                                String record = records.get(tokens[1] + " " + tokens[2]);

                                outToClient.writeBytes(record == null ?
                                        "Record not found\n" :
                                        record + "\n"
                                );
                            }

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
                                for (String record : records.keySet()) {
                                    output.append(record);
                                    output.append(" ");
                                    output.append(records.get(record));
                                    output.append("\n");
                                }

                                System.out.println(output);

                                outToClient.writeBytes(output.toString());

                                // client struggled with multiple lines so END used as a stop sequence
                                outToClient.writeBytes("END\n");
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

                    // for readability
                    System.out.println();

                    // closed socket
                    if (closed)
                        break;
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
