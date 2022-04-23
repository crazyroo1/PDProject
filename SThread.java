import java.io.*;
import java.net.*;
import java.lang.Exception;

// public class SThread extends Thread {
// 	private Object[][] RTable; // routing table
// 	private PrintWriter out, outTo; // writers (for writing back to the machine and to destination)
// 	private BufferedReader in; // reader (for reading from the machine connected to)
// 	private int inputLine, outputLine; 
// 	private String destination, addr; // communication strings
// 	private Socket inSocket;
// 	private Socket outSocket; // socket for communicating with a destination
// 	private int ind; // indext in the routing table

// 	// Constructor
// 	SThread(Object[][] Table, Socket toClient, int index) throws IOException {
// 		out = new PrintWriter(toClient.getOutputStream(), true);
// 		inSocket = toClient;
// 		in = new BufferedReader(new InputStreamReader(toClient.getInputStream()));
// 		RTable = Table;
// 		addr = toClient.getInetAddress().getHostAddress();
// 		RTable[index][0] = addr; // IP addresses
// 		RTable[index][1] = toClient; // sockets for communication
// 		ind = index;

// 		// iterate through Table and print all objects in the table
// 		for (int i = 0; i < RTable.length; i++) {
// 			for (int j = 0; j < RTable[i].length; j++) {
// 				System.out.print(RTable[i][j] + " ");
// 			}
// 			System.out.println();
// 		}
// 	}

// 	// Run method (will run for each machine that connects to the ServerRouter)
// 	public void run() {
// 		try {
// 			// Initial sends/receives
// 			destination = in.readLine(); // initial read (the destination for writing)
// 			System.out.println("Forwarding to " + destination);
// 			out.println("Connected to the router."); // confirmation of connection

// 			// waits 10 seconds to let the routing table fill with all machines' information
// 			try {
// 				Thread.currentThread().sleep(100);
// 			} catch (InterruptedException ie) {
// 				System.out.println("Thread interrupted");
// 			}

// 			// loops through the routing table to find the destination
// 			long t1 = 0, t2 = 0;
// 			t1 = System.currentTimeMillis();
// 			for (int i = 0; i < 10; i++) {
// 				if (destination.equals((String) RTable[i][0])) {
// 					outSocket = (Socket) RTable[i][1]; // gets the socket for communication from the table
// 					t2 = System.currentTimeMillis();
// 					System.out.println("Found destination: " + destination);

// 					outTo = new PrintWriter(outSocket.getOutputStream(), true); // assigns a writer
// 				}
// 			}

// 			System.out.println("Time to perform routing table lookup: " + (t2 - t1) + " ms");

// 			// send the data
// 			forwardBytesFromSourceSocketToDestinationSocket(inSocket, outSocket);

// 			// close all connections
// 			in.close();
// 			out.close();
// 			inSocket.close();
// 			outTo.close();
// 			outSocket.close();
// 		} // end try
// 		catch (IOException e) {
// 			System.err.println("Could not listen to socket.");
// 			System.exit(1);
// 		}
// 	} 

// 	public static void forwardBytesFromSourceSocketToDestinationSocket(Socket source, Socket destination) throws IOException {
// 		DataInputStream in = new DataInputStream(source.getInputStream()); // input stream
// 		DataOutputStream out = new DataOutputStream(destination.getOutputStream()); // output stream
// 		int inputLine;
// 		System.out.println("Forwarding data...");
// 		while ((inputLine = in.read()) != -1) { // reads until the end of the stream
// 			out.write(inputLine); // write the data to the destination
// 		}
// 		System.out.println("Forwarding complete.");

// 		in.close();
// 		out.close();
// 	}
// } 






enum RouteType {
	CLIENT,
	ROUTER
}

public class SThread extends Thread {
	private Socket socket;
	private PrintWriter writer;
	private BufferedReader reader;
	private Object[][] routingTable;

	// constructor that takes in a socket and a routing table
	SThread(Socket toClient, Object[][] table) throws IOException {
		socket = toClient;
		writer = new PrintWriter(toClient.getOutputStream(), true);
		reader = new BufferedReader(new InputStreamReader(toClient.getInputStream()));
		routingTable = table;
	}

	@Override
	public void run() {
		String command;
		try {
			command = reader.readLine();
			System.out.println("Command: " + command);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		switch (command) {
			case "register":
				register();
				break;
			case "registerRouter":
				registerRouter();
				break;
			case "connectToClientB":
				forwardToOtherRouter();
				break;
			case "forwardToClient":
				forwardToClient();
				break;
		}
	}

	private void register() {
		String ip;
		try {
			ip = reader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		int index = 0;
		for (int i = 0; i < routingTable.length; i++) {
			if (routingTable[i][0] == null) {
				index = i;
				break;
			}
		}

		routingTable[index][0] = ip;
		routingTable[index][1] = RouteType.CLIENT;
	}

	private void registerRouter() {
		String ip;
		try {
			ip = reader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		int index = 0;
		for (int i = 0; i < routingTable.length; i++) {
			if (routingTable[i][0] == null) {
				index = i;
				break;
			}
		}

		// get ip of reader socket
		String ipAddress = socket.getInetAddress().getHostAddress();
		routingTable[index][0] = ipAddress;
		routingTable[index][1] = RouteType.ROUTER;
	}

	private void forwardToOtherRouter() {
		String ip;
		try {
			ip = reader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		int index = 0;
		for (int i = 0; i < routingTable.length; i++) {
			if (routingTable[i][1] == RouteType.ROUTER) {
				index = i;
				break;
			}
		}

		Socket toRouter = null;
		try {
			toRouter = new Socket((String) routingTable[index][0], 5556);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		PrintWriter outToRouter = null;
		try {
			outToRouter = new PrintWriter(toRouter.getOutputStream(), true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		outToRouter.println("forwardToClient");
		outToRouter.println(ip);
	}

	private void forwardToClient() {
		String ip;
		try {
			ip = reader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		String ipAddress = socket.getInetAddress().getHostAddress();

		int index = 0;
		for (int i = 0; i < routingTable.length; i++) {
			if (routingTable[i][1] == RouteType.CLIENT) {
				index = i;
				break;
			}
		}

		Socket toClient = null;
		try {
			toClient = new Socket((String) routingTable[index][0], 5555);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		PrintWriter outToClient = null;
		try {
			outToClient = new PrintWriter(toClient.getOutputStream(), true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		outToClient.println("connectionRequest");

		outToClient.println(ipAddress);
	}
}