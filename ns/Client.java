/*
 * 
 */

import java.io.*; // Provides for system input and output through data 
                  // streams, serialization and the file system
import java.net.*; // Provides the classes for implementing networking 
                   // applications

// TCP Client class
class Client {
        public static void printHelp(){
             System.out.println(
                            "help:\n" +  
                            "\ta. No arguments\n" +
                            "\tb. Prints list of support commands and required syntax.\n" +
                            "put:\n" +
                            "\ta. Three arguments: Name, Value, and Type\n" +
                            "\tb. Add name records to database, sent to server\n" +
                            "\tc. Server records the new entry for name/type, or updates old entry\n" +
                            "del:\n" +
                            "\ta. Two arguments: name, type\n" + 
                            "\tb. Server removes a name record from database\n" + 
                            "\tc. If found, server removes the record and sends positive feedback\n" + 
                            "\td. Returns not found error message otherwise\n" + 
                            "browse:\n" + 
                            "\ta. No arguments\n" + 
                            "\tb. Retrieve all current name records in database\n" + 
                            "\tc. Returns all name and type fields of all records\n" +
                            "\td. If database is empty, return a database is empty error\n" +
                            "exit:\n" +
                            "\ta. No arguments\n" +
                            "\tb. Terminates connection with TCP server, exits\n");
        }
        
        public static void main(String argv[]) throws Exception 
        { 
                /* Client takes two input args via cmd line args
                i. Host name of machine for server
                ii. Port number server is listening at */

                String sentence; 

                String modifiedSentence; 

                // Get the server port form command line args
                int lisPort = Integer.parseInt(argv[1]);

                // Create an input stream from the System.in
                BufferedReader inFromUser = 
                new BufferedReader(new InputStreamReader(System.in));

                // Create a client socket (TCP) and connect to server
                Socket clientSocket = new Socket(argv[0], lisPort);

                // Create an output stream from the socket output stream
                DataOutputStream outToServer = 
                new DataOutputStream(clientSocket.getOutputStream()); 

                // Create an input stream from the socket input stream
                BufferedReader inFromServer = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));

                // Read in lines from input
                while (true){
                    sentence = inFromUser.readLine();
                    // Parse input string
                    String [] tokens = sentence.split("[ ]+");
                    for (int i = 0; i < tokens.length; i++)
                        System.out.println(tokens[i]);
                    // Process command according to protocol
                    switch (tokens[0]){
                        case "exit":
                            // Write exit to server so the server closes the socket
                            outToServer.writeBytes("EXIT\r\n");

                            // close the socket
                            clientSocket.close();
                            break;
                        case "put":
                        /*
                            put: 
                            a. Three args: name, value, and type
                            b. Add name records to database, sent to server
                            c. Server records the new entry for name/type, or updates old entry
                        */
                            if(tokens.length == 4){
                                sentence =  "PUT " + tokens[1] + " " + tokens[2] + " " + tokens[3];
                                
                                // send the sentence read to the server
                                outToServer.writeBytes(sentence + "\r\n");
    
                                // get the reply from the server
                                modifiedSentence = inFromServer.readLine();
    
                                // print the returned sentence
                                System.out.println("FROM SERVER: " + modifiedSentence);
                            }else
                                printHelp();

                            break;
                        case "get":
                        /*
                            get: 
                            a. Queries server database
                            b. Two arguments: name , type
                            c. Server returns record value if found
                            d. Returns not found error message if not found
                        */
                            if(tokens.length == 3){
                                sentence =  "GET " + tokens[1] + " " + tokens[2];

                                // Send the sentence read to the server
                                outToServer.writeBytes(sentence + "\r\n");
    
                                // Get the reply from the server
                                modifiedSentence = inFromServer.readLine();
    
                                // Print the returned sentence
                                System.out.println("FROM SERVER: " + modifiedSentence);
                            }else
                                printHelp();
                            break;
                        
                        case "browse":
                        /*  browse:
                            a. No args
                            b. Retrieve all current name records in database
                            c. Returns all name and type fields of all records
                            d. If database is empty, return a database is empty error
                        */
                            if(tokens.length == 1){
                                sentence = "BROWSE";
                            int lineNumber = 1;
                            // Send the sentence read to the server
                            outToServer.writeBytes(sentence + "\r\n");
    
                            // Get the reply from the server
                            // Print the returned sentence
                            System.out.println("FROM SERVER: ");
                            while((modifiedSentence = inFromServer.readLine()) != null){
                                System.out.println(lineNumber + " " + modifiedSentence);
                                lineNumber++;
                            }
                            } else
                                printHelp();
                            break;

                        case "del":
                        /*  del:
                            a. Server removes a name record from database
                            b. Two arguments: name, type
                            c. If found, removes record and sends positive feedback
                            d. Returns not found error message otherwise
                        */
                            if(tokens.length == 3){
                                sentence =  "DEL " + tokens[1] + " " + tokens[2];

                                // Send the sentence read to the server
                                outToServer.writeBytes(sentence + "\r\n");
    
                                // Get the reply from the server
                                modifiedSentence = inFromServer.readLine();
    
                                // Print the returned sentence
                                System.out.println("FROM SERVER: " + modifiedSentence);
                            }else
                                printHelp();
                            break;
                        default: 
                        //  Print list of supported commands, function, syntax
                        printHelp();
                    }
                    
                }
                
        }
}
