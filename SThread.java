import java.io.*;
import java.net.*;

public class SThread extends Thread {
	private Socket socket;
	private BufferedReader reader;
	private Route[] routingTable;

	// constructor that takes in a socket and a routing table
	SThread(Socket toClient, Route[] table) throws IOException {
		socket = toClient;
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
			if (routingTable[i] == null) {
				index = i;
				break;
			}
		}

		routingTable[index].ip = ip;
		routingTable[index].type = RouteType.CLIENT;
	}

	private void registerRouter() {
		int index = 0;
		for (int i = 0; i < routingTable.length; i++) {
			if (routingTable[i] == null) {
				index = i;
				break;
			}
		}

		// get ip of reader socket
		String ipAddress = socket.getInetAddress().getHostAddress();
		routingTable[index].ip = ipAddress;
		routingTable[index].type = RouteType.ROUTER;
	}

	private void forwardToOtherRouter() {
		int index = 0;
		for (int i = 0; i < routingTable.length; i++) {
			if (routingTable[i].type == RouteType.ROUTER) {
				index = i;
				break;
			}
		}

		Socket toRouter = null;
		try {
			toRouter = new Socket((String) routingTable[index].ip, Configuration.shared.routerPort);
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
	}

	private void forwardToClient() {
		String ipAddress = socket.getInetAddress().getHostAddress();

		int index = 0;
		for (int i = 0; i < routingTable.length; i++) {
			if (routingTable[i].type == RouteType.CLIENT) {
				index = i;
				break;
			}
		}

		Socket toClient = null;
		try {
			toClient = new Socket((String) routingTable[index].ip, Configuration.shared.clientPort);
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