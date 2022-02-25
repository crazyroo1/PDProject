import java.io.*;
import java.net.*;

public class TCPClient {
   public static void main(String[] args) throws IOException {

      // Variables for setting up connection and communication
      Socket Socket = null; // socket to connect with ServerRouter
      PrintWriter out = null; // for writing to ServerRouter
      BufferedReader in = null; // for reading form ServerRouter
      InetAddress addr = InetAddress.getLocalHost();
      String host = "8.tcp.ngrok.io";//addr.getHostAddress(); // Client machine's IP
      String routerName = "8.tcp.ngrok.io"; // ServerRouter host name
      int SockNum = 13139; // port number

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

      String address = "127.0.0.1"; //"2.tcp.ngrok.io"; // destination IP (Server)

      // Communication process (initial sends/receives
      out.println(address);// initial send (IP of the destination Server)
      String verification = in.readLine();// initial receive from router (verification of connection)
      System.out.println("ServerRouter: " + verification); // print verification

      sendBinaryFileToDestination("video.mov", Socket); // send the file to the destination
      System.out.println("File sent"); 

      // close all connections
      in.close();
      out.close();
      Socket.close();

   }

   public static void sendBinaryFileToDestination(String fileName, Socket destination) throws IOException {
      File file = new File(fileName);
      InputStream inputStream = new FileInputStream(file); // input stream for the file

      System.out.println("Reading file...");
      byte[] bytes = inputStream.readAllBytes(); // read the file
      System.out.println("File read");
      
      OutputStream outputStream = destination.getOutputStream(); // output stream for the file
      inputStream.close();
      long t1 = System.currentTimeMillis();
      System.out.println("Sending file...");
      outputStream.write(bytes); // write the file to the destination
      System.out.println("File sent");
      long t2 = System.currentTimeMillis();
      System.out.println("Time to send: " + (t2 - t1) + " ms");
      outputStream.close();
   }
}