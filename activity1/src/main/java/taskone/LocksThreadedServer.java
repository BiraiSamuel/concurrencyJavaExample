/**
  File: Server.java
  Author: Student in Fall 2020B
  Description: Server class in package taskone.
*/

package taskone;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
  
// Server class
class LocksThreadedServer<O> {
	private final ReadWriteLock readWriteLock
		= new ReentrantReadWriteLock();
	private final Lock writeLock
		= readWriteLock.writeLock();
	private final Lock readLock = readWriteLock.readLock();
	private final List<O> list = new ArrayList<>();
  
    // getElement function prints
    // i.e., read the element from the thread
    public O getElement(int i)
    {
        // acquire the thread for reading
        readLock.lock();
        try {
            System.out.println(
                "Elements by thread "
                + Thread.currentThread().getName()
                + " is printed");
            return list.get(i);
        }
        finally {
            // To unlock the acquired read thread
            readLock.unlock();
        }
    }
	
	// setElement function sets
    // i.e., write the element to the thread
    public void setElement(Socket client)
    {
		StringList strings = new StringList();
        // create a new thread object				
		Performer performer = new Performer(client, strings);
  
        // This thread will handle the client
                // separately
				// acquire the thread for writing
				writeLock.lock();
				try {
					performer.doPerform();
					System.out.println(
						"Element by thread "
						+ Thread.currentThread().getName()
						+ " is added");
				}
				finally {
					// To unlock the acquired write thread
					writeLock.unlock();
				}
    }
	
    public static void main(String[] args)
    {
        ServerSocket server = null;
		int port;
        StringList strings = new StringList();
		LocksThreadedServer<String> gfg = new LocksThreadedServer<>();

        if (args.length != 1) {
            // gradle runServer -Pport=9099 -q --console=plain
            System.out.println("Usage: gradle extraCredit");
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
				/// write the element to the thread
				gfg.setElement(client);
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