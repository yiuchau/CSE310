
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Server for Part 2
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
 *
 * Yiu Chau Lin - yiuchau.lin@stonybrook.edu
 * Brian Yang - brian.yang@stonybrook.edu
 *
 */
public class Server2 {

    public static HashMap<String, String> records = new HashMap<>();
    
    public static String type;

    public static class TCPSocket extends Thread {
        private Socket socket;

        public TCPSocket(Socket socket) {
            this.socket = socket;
        }

        /**
         * The thread method for each connection
         */
        public void run() {

            try {
                //create an input stream from the socket input stream
                BufferedReader inFromClient = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));


                // create an output stream from the socket output stream
                DataOutputStream outToClient =
                        new DataOutputStream(socket.getOutputStream());

                outToClient.writeBytes("Server is at : " + socket.getRemoteSocketAddress().toString() + "\n");

                String methodName, serverResponse;

                boolean closed = false;

                while ((methodName = inFromClient.readLine()) != null) {

                    System.out.println("Received: " + methodName);

                    methodName = methodName.split("\\s+")[1];

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
                            a. Two args: name, value
                            b. Add name records to database, sent to server
                            c. Server records the new entry for name, or updates old entry
                        */

                            String name = "";
                            String value = "";

                            /*
                                Method: PUT
                                Name: google.com
                                Type: NS
                                Value: 127.0.0.1
                             */
                            for (int i = 0; i < 2; i++) {
                                String[] line = inFromClient.readLine().split(":\\s+");

                                switch (line[0]) {
                                    case "Name":
                                        name = line[1];
                                        break;
                                    case "Value":
                                        value = line[1];
                                        break;
                                }
                            }


                            synchronized (records) {
                                System.out.println("Adding or updating record");

                                records.put(name ,value);
                            }


                            outToClient.writeBytes("Status: OK\n");
                            outToClient.writeBytes("Response: Record added\n");


                            break;
                        case "GET":
                        /*
                            get:
                            a. Queries server database
                            b. One argument: name
                            c. Server returns record value if found
                            d. Returns not found error message if not found
                        */

                            name = "";

                            /*
                                Method: GET
                                Name: google.com
                                Type: NS
                             */
                            for (int i = 0; i < 1; i++) {
                                String[] line = inFromClient.readLine().split(":\\s+");

                                switch (line[0]) {
                                    case "Name":
                                        name = line[1];
                                        break;
                                }
                            }

                            synchronized (records) {
                                String record = records.get(name);

                                outToClient.writeBytes("Status: " +
                                        (record == null ? "FAIL\n" : "OK\n")
                                );

                                outToClient.writeBytes("Response: " + (record == null ?
                                        "Record not found\n" :
                                        record + "\n")
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

                                outToClient.writeBytes("Status: OK\n");
                                outToClient.writeBytes("Lines: " + records.size() + "\n");

                                for (String record : records.keySet()) {
                                    output.append(record);
                                    output.append(" ");
                                    output.append(records.get(record));
                                    output.append("\n");
                                }

                                System.out.println(output);

                                outToClient.writeBytes(output.toString());
                            }

                            break;

                        case "DEL":
                        /*  del:
                            a. Server removes a name record from database
                            b. One argument: name
                            c. If found, removes record and sends positive feedback
                            d. Returns not found error message otherwise
                        */

                            name = "";

                            /*
                                Method: DEL
                                Name: google.com
                             */
                            for (int i = 0; i < 1; i++) {
                                String[] line = inFromClient.readLine().split(":\\s+");

                                switch (line[0]) {
                                    case "Name":
                                        name = line[1];
                                        break;
                                }
                            }

                            synchronized (records) {
                                String record = records.remove(name);

                                outToClient.writeBytes("Status: " +
                                        (record == null ? "FAIL\n" : "OK\n")
                                );

                                outToClient.writeBytes("Response: " + (record == null ?
                                        "Record not found\n" :
                                        "Record deleted successfully\n")
                                );
                            }

                            break;
                    }

                    // for readability
                    System.out.println();

                    // closed socket
                    if (closed){
                        
                        try {
                            FileOutputStream fileOut
                                    = new FileOutputStream(type + "records.ser");
                            ObjectOutputStream out = new ObjectOutputStream(fileOut);
                            out.writeObject(records);
                            out.close();
                            fileOut.close();
                            System.out.printf("Serialized data is saved in records.ser");
                        } catch (IOException i) {
                            i.printStackTrace();
                        }
                        break;
                    }
                        
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] argv) throws Exception {
        
        // Server2 takes one argument: type to access
        
        type = argv[0];

        // create a server socket (TCP)
        ServerSocket welcomeSocket = new ServerSocket(0);

        // single line stdout of port for Manager to read
        System.out.println(welcomeSocket.getLocalPort());
        System.out.println("Server is at port: " + welcomeSocket.getLocalPort());
        
        String line = null;
        
        try {
            
            URL path = Server2.class.getResource(type + "records.ser");
            
            if(path != null){
                File file = new File(path.getFile());

                if (file.exists()) {
                    System.out.println("Records read from file.");
                    FileInputStream fis = new FileInputStream(file);
                    ObjectInputStream ois = new ObjectInputStream(fis);

                    records = (HashMap<String, String>) ois.readObject();

                    ois.close();
                    fis.close();
                }  
            }
            
           
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        
        // loop infinitely (process clients sequentially)
        while (true) {
            // Wait and accept client connection
            Socket connectionSocket = welcomeSocket.accept();

            new TCPSocket(connectionSocket).start();
        }
    }
}
