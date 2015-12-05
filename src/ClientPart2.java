/*
 * 
 */

import java.io.*; // Provides for system input and output through data 
// streams, serialization and the file system
import java.net.*; // Provides the classes for implementing networking 
// applications

/**
 *
 * TCP Client
 *
 * Request messages: Method: PUT/GET/BROWSE/DEL/EXIT Name: test.com Type: A/NS
 *
 * Response message: Status: OK/FAIL Response: 192.168.1.1 (value of record)
 */
class ClientPart2 {

    /**
     * flag indicating whether a command is valid or not, connection is open or
     * closed
     */
    public static boolean validCmd = true, closed = false;

    public static void managerHelp() {
        System.out.println(
            "help:\n"
                    + "\ta. No arguments\n"
                    + "\tb. Prints list of supported commands and required syntax.\n"
                    + "type:\n"
                    + "\ta. One argumen: Record type\n"
                    + "\tb. Contacts manager to obtain address of server containing type requested\n"
                    + "\tc. Returns error condition 'type not found'\n"
                    + "exit:\n"
                    + "\ta. No arguments\n"
                    + "\tb. Closes connection with Manager server.");
    }

    public static void printHelp() {

        // if help is shown, a command was not sent to the server
        validCmd = false;

        System.out.println(
                "help:\n"
                + "\ta. No arguments\n"
                + "\tb. Prints list of support commands and required syntax.\n"
                +"get:\n"
                +"\ta. Queries server database\n"
                +"\tb. One arguments: name\n"
                +"\tc. Server returns record value if found\n"
                +"\td. Returns not found error message if not found\n"
                + "put:\n"
                + "\ta. Two arguments: Name, and Value\n"
                + "\tb. Add name records to database, sent to server\n"
                + "\tc. Server records the new entry for name/type, or updates old entry\n"
                + "del:\n"
                + "\ta. One argument: name\n"
                + "\tb. Server removes a name record from database\n"
                + "\tc. If found, server removes the record and sends positive feedback\n"
                + "\td. Returns not found error message otherwise\n"
                + "browse:\n"
                + "\ta. No arguments\n"
                + "\tb. Retrieve all current name records in database\n"
                + "\tc. Returns all name and type fields of all records\n"
                + "\td. If database is empty, return a database is empty error\n"
                + "done:\n"
                + "\ta. No arguments\n"
                + "\tb. Terminates connection with TCP server, exits\n");
    }

    public static void main(String argv[]) throws Exception {
        /*
            Client: 
	1. Takes host name and port of manager as arguments
	2. Prompts for record type
	3. Send type to manager
	4. Manager returns address of name server
	5. Client establishes connection to name server
	6. Commands and queries based on stage 1, except no type field.
         */
        String toManager, managerReturn;
        String managerAddress = argv[0];
        int lisPort = Integer.parseInt(argv[1]);

        // Create an input stream from the System.in
        BufferedReader inFromUser
                = new BufferedReader(new InputStreamReader(System.in));


        try {
            // Create a client socket (TCP) and connect to manager
            Socket clientSocket = new Socket(managerAddress, lisPort);


            // Create an output stream from the socket output stream
            DataOutputStream outToManager
                    = new DataOutputStream(clientSocket.getOutputStream());

            // Create an input stream from the socket input stream
            BufferedReader inFromManager = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));


            while (true) {
                System.out.print("Enter type to access: \n>");
                toManager = inFromUser.readLine();

                String[] tokens = toManager.split("[ ]+");

                switch (tokens[0]) {
                    case "exit":
                        // Write exit to server so the server closes the socket
                        outToManager.writeBytes("Method: EXIT\r\n");

                        // close the socket
                        clientSocket.close();
                        closed = true;
                        break;
                    case "type":

                        if (tokens.length == 2) {

                            outToManager.writeBytes("Method: TYPE\r\n");
                            outToManager.writeBytes("Request: " + tokens[1] + "\r\n");

                            String responseCode = inFromManager.readLine().split(":\\s+")[1];

                            if (responseCode.equals("OK")) {
                                String response = inFromManager.readLine().split(":\\s+")[1];
                                System.out.println("Response - Server of type " + toManager + " is at port " + response);
                                connectServer(argv[0], response);

                            } else {
                                String errorMessage = inFromManager.readLine().split(":\\s+")[1];
                                System.out.println("Error - " + errorMessage);
                            }
                        }
                        break;
                    default:
                          managerHelp();
                }

                if (closed) {
                    System.out.println("Connection with server terminated.\n");
                    break;
                }

            }
        } catch (ConnectException e) {
            System.out.println("Error connecting to server. The hostname and/or port number is correct.");
        }
    }

    public static void connectServer(String serverAddress, String serverPort) throws Exception {
        /* Client takes two input args
                i. Host name of machine for server
                ii. Port number server is listening at */

        String sentence;

        String modifiedSentence;

        // Get the server port from args
        int lisPort = Integer.parseInt(serverPort);

        // Create an input stream from the System.in
        BufferedReader inFromUser
                = new BufferedReader(new InputStreamReader(System.in));

        // Create a client socket (TCP) and connect to server
        Socket clientSocket2 = new Socket(serverAddress, lisPort);

        // Create an output stream from the socket output stream
        DataOutputStream outToServer
                = new DataOutputStream(clientSocket2.getOutputStream());

        // Create an input stream from the socket input stream
        BufferedReader inFromServer = new BufferedReader(
                new InputStreamReader(clientSocket2.getInputStream()));

        // display welcome and port
        System.out.println("Successfully connected - " + inFromServer.readLine());

        // indicating multiple lines needed
        boolean browse = false;

        boolean closed = false;

        // Read in lines from input
        while (true) {
            System.out.print("> ");
            sentence = inFromUser.readLine();
            // Parse input string
            String[] tokens = sentence.split("[ ]+");
//                    for (String token : tokens)
//                        System.out.println(token);
            // Process command according to protocol
            switch (tokens[0]) {
                case "done":
                    // Write exit to server so the server closes the socket
                    outToServer.writeBytes("Method: EXIT\r\n");

                    // close the socket
                    clientSocket2.close();
                    closed = true;
                    break;
                case "put":
                    /*
                            put: 
                            a. Two args: name, value
                            b. Add name records to database, sent to server
                            c. Server records the new entry for name, or updates old entry
                     */
                    if (tokens.length == 3) {
                        sentence = "PUT " + tokens[1] + " " + tokens[2];

                        // send the sentence read to the server
//                                outToServer.writeBytes(sentence + "\r\n");
                        outToServer.writeBytes("Method: PUT\n");
                        outToServer.writeBytes("Name: " + tokens[1] + "\n");
                        outToServer.writeBytes("Value: " + tokens[2] + "\n");

                        // get the reply from the server
//                                modifiedSentence = inFromServer.readLine();
//
//                                // print the returned sentence
//                                System.out.println("FROM SERVER: " + modifiedSentence);
                    } else {
                        printHelp();
                    }

                    break;
                case "get":
                    /*
                            get: 
                            a. Queries server database
                            b. Two arguments: name
                            c. Server returns record value if found
                            d. Returns not found error message if not found
                     */
                    if (tokens.length == 2) {
                        sentence = "GET " + tokens[1];

                        outToServer.writeBytes("Method: GET\n");
                        outToServer.writeBytes("Name: " + tokens[1] + "\n");

//                                // Get the reply from the server
//                                modifiedSentence = inFromServer.readLine();
//
//                                // Print the returned sentence
//                                System.out.println("FROM SERVER: " + modifiedSentence);
                    } else {
                        printHelp();
                    }
                    break;

                case "browse":
                    /*  browse:
                            a. No args
                            b. Retrieve all current name records in database
                            c. Returns all name and type fields of all records
                            d. If database is empty, return a database is empty error
                     */
                    if (tokens.length == 1) {
                        sentence = "Method: BROWSE";
                        int lineNumber = 1;
                        // Send the sentence read to the server
                        outToServer.writeBytes(sentence + "\r\n");

                        browse = true;

                        // Get the reply from the server
                        // Print the returned sentence
//                            System.out.println("FROM SERVER: ");
//                            while(!(modifiedSentence = inFromServer.readLine()).equals("END")){
//                                System.out.println(lineNumber + " " + modifiedSentence);
//                                lineNumber++;
//                            }
                    } else {
                        printHelp();
                    }
                    break;

                case "del":
                    /*  del:
                            a. Server removes a name record from database
                            b. Two arguments: name
                            c. If found, removes record and sends positive feedback
                            d. Returns not found error message otherwise
                     */
                    if (tokens.length == 2) {
                        sentence = "DEL " + tokens[1];

                        outToServer.writeBytes("Method: DEL\n");
                        outToServer.writeBytes("Name: " + tokens[1] + "\n");

//                                // Get the reply from the server
//                                modifiedSentence = inFromServer.readLine();
//
//                                // Print the returned sentence
//                                System.out.println("FROM SERVER: " + modifiedSentence);
                    } else {
                        printHelp();
                    }
                    break;
                default:
                    //  Print list of supported commands, function, syntax
                    printHelp();
            }

            if (closed) {
                System.out.println("Connection with server terminated.\n");
                break;
            }

            // don't read response if there wasn't a good cmd to begin with
            if (validCmd) {
                String responseCode = inFromServer.readLine().split(":\\s+")[1];

                if (responseCode.equals("OK")) {

                    if (browse) {
                        int lines = Integer.parseInt(inFromServer.readLine().split(":\\s+")[1]);

                        for (int i = 0; i < lines; i++) {
                            System.out.println(i + " - " + inFromServer.readLine());
                        }

                        // reset browse flag
                        browse = false;

                    } else {
                        String response = inFromServer.readLine().split(":\\s+")[1];
                        System.out.println("Response - " + response);
                    }
                } else {
                    String errorMessage = inFromServer.readLine().split(":\\s+")[1];
                    System.out.println("Error - " + errorMessage);
                }
            } else {
                validCmd = true;
            }

            // newline for readability
            System.out.println();

        }

    }
}
