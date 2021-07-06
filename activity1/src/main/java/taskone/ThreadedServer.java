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
class ThreadedServer {
    public static void main(String[] args)
    {
        ServerSocket server = null;
		int port;
        StringList strings = new StringList();

        if (args.length != 1) {
            // gradle runServer -Pport=9099 -q --console=plain
            System.out.println("Usage: gradle runTask1 -Pport=9099 -q --console=plain");
            System.exit(1);
        }
        port = -1;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException nfe) {
            System.out.println("[Port] must be an integer");
            System.exit(2);
        }
  
        try {
  
            // server is listening on port
            server = new ServerSocket(port);
            server.setReuseAddress(true);
  
            // running infinite loop for getting
            // client request
            while (true) {
  
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