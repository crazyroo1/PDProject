import java.io.*;
import java.net.*;
import java.lang.Exception;

public class SThread extends Thread {
	private Object[][] RTable; // routing table
	private PrintWriter out, outTo; // writers (for writing back to the machine and to destination)
	private BufferedReader in; // reader (for reading from the machine connected to)
	private int inputLine, outputLine; 
	private String destination, addr; // communication strings
	private Socket inSocket;
	private Socket outSocket; // socket for communicating with a destination
	private int ind; // indext in the routing table

	// Constructor
	SThread(Object[][] Table, Socket toClient, int index) throws IOException {
		out = new PrintWriter(toClient.getOutputStream(), true);
		inSocket = toClient;
		in = new BufferedReader(new InputStreamReader(toClient.getInputStream()));
		RTable = Table;
		addr = toClient.getInetAddress().getHostAddress();
		RTable[index][0] = addr; // IP addresses
		RTable[index][1] = toClient; // sockets for communication
		ind = index;

		// iterate through Table and print all objects in the table
		for (int i = 0; i < RTable.length; i++) {
			for (int j = 0; j < RTable[i].length; j++) {
				System.out.print(RTable[i][j] + " ");
			}
			System.out.println();
		}
	}

	// Run method (will run for each machine that connects to the ServerRouter)
	public void run() {
		try {
			// Initial sends/receives
			destination = in.readLine(); // initial read (the destination for writing)
			System.out.println("Forwarding to " + destination);
			out.println("Connected to the router."); // confirmation of connection

			// waits 10 seconds to let the routing table fill with all machines' information
			try {
				Thread.currentThread().sleep(100);
			} catch (InterruptedException ie) {
				System.out.println("Thread interrupted");
			}

			// loops through the routing table to find the destination
			for (int i = 0; i < 10; i++) {
				if (destination.equals((String) RTable[i][0])) {
					outSocket = (Socket) RTable[i][1]; // gets the socket for communication from the table
					System.out.println("Found destination: " + destination);

					outTo = new PrintWriter(outSocket.getOutputStream(), true); // assigns a writer
				}
			}

			// send the data
			forwardBytesFromSourceSocketToDestinationSocket(inSocket, outSocket);

			// close all connections
			in.close();
			out.close();
			inSocket.close();
			outTo.close();
			outSocket.close();
		} // end try
		catch (IOException e) {
			System.err.println("Could not listen to socket.");
			System.exit(1);
		}
	} 

	public static void forwardBytesFromSourceSocketToDestinationSocket(Socket source, Socket destination) throws IOException {
		DataInputStream in = new DataInputStream(source.getInputStream());
		DataOutputStream out = new DataOutputStream(destination.getOutputStream());
		int inputLine;
		System.out.println("Forwarding data...");
		while ((inputLine = in.read()) != -1) {
			out.write(inputLine);
		}
		System.out.println("Forwarding complete.");
	}
} 