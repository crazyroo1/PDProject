import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class TCPServer {
   public static void main(String[] args) throws IOException {

      // Variables for setting up connection and communication
      Socket Socket = null; // socket to connect with ServerRouter
      PrintWriter out = null; // for writing to ServerRouter
      BufferedReader in = null; // for reading form ServerRouter
      InetAddress addr = InetAddress.getLocalHost();
      String host = addr.getHostAddress(); // Server machine's IP
      // String host = "2.tcp.ngrok.io";
      String routerName = "127.0.0.1"; // ServerRouter host name
      int SockNum = 5555; // port number
      InputStreamReader inReader;
      // Tries to connect to the ServerRouter
      try {
         Socket = new Socket(routerName, SockNum);
         out = new PrintWriter(Socket.getOutputStream(), true);
         in = new BufferedReader(new InputStreamReader(Socket.getInputStream()));
      } catch (UnknownHostException e) {
         System.err.println("Don't know about router: " + routerName);
         System.exit(1);
      } catch (IOException e) {
         System.err.println("Couldn't get I/O for the connection to: " + routerName);
         System.exit(1);
      }


      inReader = new InputStreamReader(Socket.getInputStream());
      // Variables for message passing
      String fromServer; // messages sent to ServerRouter
      String fromClient; // messages received from ServerRouter
      String address = "127.0.0.1"; // destination IP (Client)

      // Communication process (initial sends/receives)
      out.println(address);// initial send (IP of the destination Client)
      fromClient = in.readLine();// initial receive from router (verification of connection)
      System.out.println("ServerRouter: " + fromClient);



      receiveBytesAndWriteToFile(Socket, "downloadedFile.mov");

      // close all connections
      in.close();
      out.close();
      Socket.close();
   }

   public static void receiveBytesAndWriteToFile(Socket source, String filename) {
      try {
         InputStream inputStream = source.getInputStream(); // get the input stream
         OutputStream outputStream = new FileOutputStream(filename); // get the output stream

         int byteRead = -1;
         int byteCount = 0;
         // read the data and write to file

         ArrayList<Integer> byteArray = new ArrayList<Integer>();

         System.out.println("Waiting for data...");
         boolean receivingBegan = false;
         while ((byteRead = inputStream.read()) != -1) { // read the file
            if (!receivingBegan) {
               receivingBegan = true;
               System.out.println("Receiving file...");
            }

            byteArray.add(byteRead); // add the byte to the array
            byteCount++; // increment the byte count

            double megabyteCount = byteCount / 1000000.0; // calculate the megabyte count
            if(megabyteCount % 1 == 0) { // if the megabyte count is a whole number
               System.out.println("Received " + megabyteCount + " MB"); // print the megabyte count
            }
         }
         System.out.println("File received");

         System.out.println("Writing file...");
         byte[] bytes = new byte[byteArray.size()]; 
         for(int i = 0; i < byteArray.size(); i++) { 
            bytes[i] = byteArray.get(i).byteValue(); // convert the arraylist to a byte array
         }

         outputStream.write(bytes); // write to file
         System.out.println("File written");
         outputStream.close(); // close the output stream
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}
