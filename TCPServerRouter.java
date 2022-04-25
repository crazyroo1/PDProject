import java.net.*;
import java.io.*;

public class TCPServerRouter {
   public static void main(String[] args) {
      Route[] routingTable = new Route[10]; // routing table
      
      ServerSocket serverSocket = null; // server socket for accepting connections
      try {
         serverSocket = new ServerSocket(Configuration.shared.routerPort);
      } catch (IOException e) {
         System.err.println("Could not listen on port: 5556.");
         System.exit(1);
      }

      try {
         System.out.println(InetAddress.getLocalHost().getHostName());
      } catch (UnknownHostException e) {
         System.err.println("Could not get local host name.");
         System.exit(1);
      }

      RegisterWithOtherRouter registerWithOtherRouter = new RegisterWithOtherRouter(Configuration.shared.otherRouterIP);
      registerWithOtherRouter.start();

      while(true) {
         try {
            Socket socket = serverSocket.accept();
            System.out.println("New connection from: " + socket.getInetAddress().getHostAddress());
            SThread thread = new SThread(socket, routingTable);
            thread.start();
         } catch (IOException e) {
            System.err.println("Client/Server failed to connect.");
            System.exit(1);
         }
      }
   }
}