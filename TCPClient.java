import java.io.*;
import java.net.*;

// public class TCPClient {
//    public static void main(String[] args) throws IOException {

//       // Variables for setting up connection and communication
//       Socket Socket = null; // socket to connect with ServerRouter
//       PrintWriter out = null; // for writing to ServerRouter
//       BufferedReader in = null; // for reading form ServerRouter
//       InetAddress addr = InetAddress.getLocalHost();
//       String host = "8.tcp.ngrok.io";//addr.getHostAddress(); // Client machine's IP
//       String routerName = "142.44.163.24"; // ServerRouter host name
//       int SockNum = 5555; // port number

//       // Tries to connect to the ServerRouter
//       try {
//          Socket = new Socket(routerName, SockNum);
//          out = new PrintWriter(Socket.getOutputStream(), true);
//          in = new BufferedReader(new InputStreamReader(Socket.getInputStream()));
//       } catch (UnknownHostException e) {
//          System.err.println("Don't know about router: " + routerName);
//          System.exit(1);
//       } catch (IOException e) {
//          System.err.println("Couldn't get I/O for the connection to: " + routerName);
//          System.exit(1);
//       }

//       String address = "127.0.0.1"; //"2.tcp.ngrok.io"; // destination IP (Server)

//       // Communication process (initial sends/receives
//       out.println(address);// initial send (IP of the destination Server)
//       String verification = in.readLine();// initial receive from router (verification of connection)
//       System.out.println("ServerRouter: " + verification); // print verification

//       sendBinaryFileToDestination("image.jpeg", Socket); // send the file to the destination
//       System.out.println("File sent"); 

//       // close all connections
//       in.close();
//       out.close();
//       Socket.close();

//    }

//    public static void sendBinaryFileToDestination(String fileName, Socket destination) throws IOException {
//       File file = new File(fileName);
//       InputStream inputStream = new FileInputStream(file); // input stream for the file

//       System.out.println("Reading file...");
//       byte[] bytes = inputStream.readAllBytes(); // read the file
//       inputStream.close(); // close the input stream
//       System.out.println("File read");
      
//       OutputStream outputStream = destination.getOutputStream(); // output stream for the file
//       long t1 = System.currentTimeMillis(); // start time
//       System.out.println("Sending file...");
//       outputStream.write(bytes); // write the file to the destination
//       System.out.println("File sent"); 
//       long t2 = System.currentTimeMillis(); // end time
//       System.out.println("Time to send: " + (t2 - t1) + " ms"); 
//       outputStream.close(); // close the output stream
//    }
// }

enum ClientIdentifier {
   A, // Initial sender
   B, // Initial receiver
}
public class TCPClient {
   // Things to edit before compiling
   static String routerIP = ""; 
   static ClientIdentifier clientID = ClientIdentifier.A;
   
   static String myIP;
   static Socket socket = null;
   public static void main(String[] args) throws UnknownHostException, IOException {
      myIP = InetAddress.getLocalHost().getHostAddress();
      routerIP = myIP;

      boolean onPi = false;
      try {
         if (InetAddress.getLocalHost().getHostName().equals("turnerpi4")) {
            onPi = true;
         }
      } catch (UnknownHostException e) {
         System.err.println("Could not get local host name.");
         System.exit(1);
      }

      clientID = onPi ? ClientIdentifier.B : ClientIdentifier.A;

      registerWithRouter();

      switch (clientID) {
         case A:
            // Request B connects to me, then wait for B to connect to me
            sendRequestToClientB();
            socket = waitForAndAcceptIncomingSocketConnection();

            sendString("Hello from A");
            String uppercasedResponse = receiveString();
            System.out.println("Received: " + uppercasedResponse);
            break;
         case B:
            // Wait for request, then connect to A
            String destinationIP = waitForRequestFromClientA();
            connectToClientA(destinationIP);

            String initialString = receiveString();
            System.out.println("Received: " + initialString);
            repeatStringAsUppercasedAndSendBack(initialString);
            break;
      }
   }

   public static void sendRequestToClientB() throws UnknownHostException, IOException {
      // Send request to Client B
      Socket routerSocket = new Socket(routerIP, 5556);
      PrintWriter out = new PrintWriter(routerSocket.getOutputStream(), true);
      out.println("connectToClientB");
      out.println(myIP);
   }

   // returns socket to Client B
   public static Socket waitForAndAcceptIncomingSocketConnection() {
      // Wait for incoming socket connection from Client B
      Socket socket;
      try {
         ServerSocket serverSocket = new ServerSocket(5555);
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
      ServerSocket serverSocket = new ServerSocket(5555);
      System.out.println("Waiting for request from Client A...");
      Socket socket = serverSocket.accept();
      serverSocket.close();
      // read input from router
      BufferedReader in = null;
      try {
         in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      } catch (IOException e) {
         System.err.println("Couldn't get I/O for the connection to: " + routerIP);
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

   public static void connectToClientA(String ip) {
      // Connect to Client A
      try {
         socket = new Socket(ip, 5555);
         System.out.println("Connected to Client A");
      } catch (IOException e) {
         System.err.println("Couldn't connect to Client A");
         System.exit(1);
      }
   }

   public static void sendString(String str) {
      try {
         PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
         out.println(str);
      } catch (IOException e) {
         System.err.println("Couldn't get I/O for the connection to: " + routerIP);
         System.exit(1);
      }
   }

   public static String receiveString() {
      // Receive lowercase string
      try {
         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         String str = in.readLine();
         return str;
      } catch (IOException e) {
         System.err.println("Couldn't get I/O for the connection to: " + routerIP);
         System.exit(1);
      }
      return null;
   }

   public static void repeatStringAsUppercasedAndSendBack(String str) {
      // Repeat string as uppercased and send back
      String uppercasedStr = str.toUpperCase();
      sendString(uppercasedStr);
   }

   public static void registerWithRouter() throws UnknownHostException, IOException {
      // Register with router
      Socket routerSocket = new Socket(routerIP, 5556);
      PrintWriter out = new PrintWriter(routerSocket.getOutputStream(), true);
      out.println("register");
      out.println(myIP);
   }
}