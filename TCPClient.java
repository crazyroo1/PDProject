import java.io.*;
import java.net.*;

enum ClientIdentifier {
   A, // Initial sender
   B, // Initial receiver
}
public class TCPClient {
   // Things to edit before compiling
   static ClientIdentifier clientID;
   
   static String localhost;
   public static void main(String[] args) throws UnknownHostException, IOException {
      localhost = InetAddress.getLocalHost().getHostAddress();

      clientID = Configuration.shared.onPi ? ClientIdentifier.B : ClientIdentifier.A;

      registerWithRouter();

      Socket socket;

      switch (clientID) {
         case A:
            // Request B connects to me, then wait for B to connect to me
            sendRequestToClientB();
            socket = waitForAndAcceptIncomingSocketConnection();

            sendString("Hello from A", socket);
            String uppercasedResponse = receiveString(socket);
            System.out.println("Received: " + uppercasedResponse);
            break;
         case B:
            // Wait for request, then connect to A
            String destinationIP = waitForRequestFromClientA();
            socket = connectToClientA(destinationIP);

            String initialString = receiveString(socket);
            System.out.println("Received: " + initialString);
            repeatStringAsUppercasedAndSendBack(initialString, socket);
            break;
         default:
            socket = null;
      }

      socket.close();
   }

   public static void sendRequestToClientB() throws UnknownHostException, IOException {
      // Send request to Client B
      Socket routerSocket = new Socket(localhost, Configuration.shared.routerPort);
      PrintWriter out = new PrintWriter(routerSocket.getOutputStream(), true);
      out.println("connectToClientB");
      routerSocket.close();
   }

   // returns socket to Client B
   public static Socket waitForAndAcceptIncomingSocketConnection() {
      // Wait for incoming socket connection from Client B
      Socket socket;
      try {
         ServerSocket serverSocket = new ServerSocket(Configuration.shared.clientPort);
         System.out.println("Waiting for incoming socket connection from Client B...");
         socket = serverSocket.accept();
         serverSocket.close();
         System.out.println("Incoming socket connection from Client B accepted");
      } catch (IOException e) {
         System.err.println("Couldn't start up server socket");
         System.exit(1);
         return null;
      }
      
      return socket;
   }

   // returns ip of client A
   public static String waitForRequestFromClientA() throws UnknownHostException, IOException {
      ServerSocket serverSocket = new ServerSocket(Configuration.shared.clientPort);
      System.out.println("Waiting for request from Client A...");
      Socket socket = serverSocket.accept();
      serverSocket.close();
      // read input from router
      BufferedReader in = null;
      try {
         in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      } catch (IOException e) {
         System.err.println("Couldn't get I/O for the connection to: " + localhost);
         System.exit(1);
      }

      String command = in.readLine();
      if (!command.equals("connectionRequest")) {
         System.err.println("Unexpected command: " + command);
         System.exit(1);
      }
      
      String destinationIP = "";
      try {
         destinationIP = in.readLine();
      } catch (IOException e) {
         System.err.println("Couldn't read from router");
         System.exit(1);
      }

      socket.close();

      return destinationIP;
   }

   public static Socket connectToClientA(String ip) {
      // Connect to Client A
      try {
         Socket socket = new Socket(ip, Configuration.shared.clientPort);
         System.out.println("Connected to Client A");
         return socket;
      } catch (IOException e) {
         System.err.println("Couldn't connect to Client A");
         System.exit(1);
      }
      return null;
   }

   public static void sendString(String str, Socket socket) {
      try {
         PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
         out.println(str);
      } catch (IOException e) {
         System.err.println("Couldn't get I/O for the connection to: " + localhost);
         System.exit(1);
      }
   }

   public static String receiveString(Socket socket) {
      // Receive lowercase string
      try {
         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         String str = in.readLine();
         return str;
      } catch (IOException e) {
         System.err.println("Couldn't get I/O for the connection to: " + localhost);
         System.exit(1);
      }
      return null;
   }

   public static void repeatStringAsUppercasedAndSendBack(String str, Socket socket) {
      // Repeat string as uppercased and send back
      String uppercasedStr = str.toUpperCase();
      sendString(uppercasedStr, socket);
   }

   public static void registerWithRouter() throws UnknownHostException, IOException {
      // Register with router
      Socket routerSocket = new Socket(localhost, Configuration.shared.routerPort);
      PrintWriter out = new PrintWriter(routerSocket.getOutputStream(), true);
      out.println("register");
      out.println(localhost);
      routerSocket.close();
   }
}