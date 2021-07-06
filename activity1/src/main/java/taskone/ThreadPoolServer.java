/**
  File: Server.java
  Author: Student in Fall 2020B
  Description: Server class in package taskone.
*/

package taskone;
import java.io.*;
import java.net.*;
import org.json.JSONObject;
  
// Server class
class ThreadedPoolServer {
    public static void main(String[] args)
    {
        ServerSocket server = null;
		int port;
		int threadNumber;
        StringList strings = new StringList();

        if (args.length != 2) {
            // gradle runServer -Pport=9099 -q --console=plain
            System.out.println("Usage: gradle runTask2 -Pport=9099 -q --console=plain -Pno=00");
            System.exit(1);
        }
        port = -1;
		threadNumber = 0;
        try {
            port = Integer.parseInt(args[1]);
			threadNumber = Integer.parseInt(args[0]);
        } catch (NumberFormatException nfe) {
            System.out.println("[Port] must be an integer");
            System.exit(2);
        }
  
        try {
			int counter=0;
  
            // server is listening on port
            server = new ServerSocket(port);
            server.setReuseAddress(true);
  
            // running infinite loop for getting
            // client request
            while (counter<threadNumber) {
				counter++;
  
                // socket object to receive incoming client
                // requests
                Socket client = server.accept();
  
                // Displaying that new client is connected
                // to server
                System.out.println("New client connected"
                                   + client.getInetAddress()
                                         .getHostAddress());
  
                // create a new thread object				
				Performer performer = new Performer(client, strings);
  
                // This thread will handle the client
                // separately
                new Thread(() -> {
					// code goes here.
					performer.doPerform();
				}).start();
				try {
					System.out.println("close socket of client ");
					client.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (server != null) {
                try {
                    server.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}