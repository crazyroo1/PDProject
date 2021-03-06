import java.io.*;
import java.net.*;
import java.util.ArrayList;

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

            // read data from data.txt
            long readT1 = System.currentTimeMillis();
            byte[] data = readData();
            long readT2 = System.currentTimeMillis();
            System.out.println("Read data from data.txt in " + (readT2 - readT1) + " ms.");

            // send data to B
            long sendT1 = System.currentTimeMillis();
            sendData(data, socket);
            long sendT2 = System.currentTimeMillis();
            System.out.println("Sent data to client in " + (sendT2 - sendT1) + " ms.");

            break;
         case B:
            // Wait for request, then connect to A
            String destinationIP = waitForRequestFromClientA();
            socket = connectToClientA(destinationIP);

            // read data from A
            long receiveT1 = System.currentTimeMillis();
            byte[] dataFromA = receiveData(socket);
            long receiveT2 = System.currentTimeMillis();
            System.out.println("Received data from client in " + (receiveT2 - receiveT1) + " ms.");

            // write data to downloaded.txt
            long writeT1 = System.currentTimeMillis();
            saveData(dataFromA);
            long writeT2 = System.currentTimeMillis();
            System.out.println("Wrote data to downloaded.txt in " + (writeT2 - writeT1) + " ms.");

            // check if downloaded.txt is the same as data.txt
            checkDataIntegrity(dataFromA);
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

   public static byte[] readData() {
      FileInputStream fis = null;
      try {
         fis = new FileInputStream("data.txt");
      } catch (FileNotFoundException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
         System.exit(1);
      }

      byte[] data = null;
      try {
         data = fis.readAllBytes();
         fis.close();
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
         System.exit(1);
      }

      return data;
   }

   public static void sendData(byte[] data, Socket socket) {
      try {
         OutputStream out = socket.getOutputStream();
         out.write(data);
      } catch (IOException e) {
         System.err.println("Couldn't send data");
         System.exit(1);
      }
   }

   public static byte[] receiveData(Socket socket) {
      ArrayList<Byte> data = new ArrayList<Byte>();
      try {
         InputStream in = socket.getInputStream();
         int b;
         while ((b = in.read()) != -1) {
            data.add((byte) b);
         }
      } catch (IOException e) {
         System.err.println("Couldn't receive data");
         System.exit(1);
      }

      byte[] bytes = new byte[data.size()]; 
      for(int i = 0; i < data.size(); i++) { 
         bytes[i] = data.get(i).byteValue(); // convert the arraylist to a byte array
      }
      return bytes;
   }

   public static void saveData(byte[] data) {
      FileOutputStream fos = null;
      try {
         fos = new FileOutputStream("downloaded.txt");
      } catch (FileNotFoundException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

      try {
         fos.write(data);
         fos.close();
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   public static void checkDataIntegrity(byte[] data) throws IOException {
      FileInputStream fis2 = new FileInputStream("data.txt");
      byte[] originalData = fis2.readAllBytes();
      fis2.close();
      
      boolean same = true;
      if (originalData.length != data.length) {
         same = false;
         System.out.println("Downloaded file is not the same as data.txt");
      } else {
         for (int i = 0; i < originalData.length; i++) {
            if (originalData[i] != data[i]) {
               same = false;
               System.out.println("Downloaded file is not the same as data.txt");
               break;
            }
         }
      }

      if (same) {
         System.out.println("Downloaded file is the same as data.txt");
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