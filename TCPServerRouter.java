import java.net.*;
import java.io.*;

// public class TCPServerRouter {
//    public static void main(String[] args) throws IOException {
//       Socket clientSocket = null; // socket for the thread
//       Object[][] routingTable = new Object[10][2]; // routing table
//       int SockNum = 5555; // port number
//       Boolean Running = true;
//       int ind = 0; // indext in the routing table

//       // Accepting connections
//       ServerSocket serverSocket = null; // server socket for accepting connections
//       try {
//          serverSocket = new ServerSocket(5555);
//          System.out.println("ServerRouter is Listening on port: 5555.");
//       } catch (IOException e) {
//          System.err.println("Could not listen on port: 5555.");
//          System.exit(1);
//       }

//       // Creating threads with accepted connections
//       while (Running == true) {
//          try {
//             clientSocket = serverSocket.accept();
//             SThread t = new SThread(routingTable, clientSocket, ind); // creates a thread with a random port
//             t.start(); // starts the thread
//             ind++; // increments the index
//             System.out.println("ServerRouter connected with Client/Server: " + clientSocket.getInetAddress().getHostAddress());
//          } catch (IOException e) {
//             System.err.println("Client/Server failed to connect.");
//             System.exit(1);
//          }
//       } // end while

//       // closing connections
//       clientSocket.close();
//       serverSocket.close();

//    }
// }

public class TCPServerRouter {
   public static void main(String[] args) {
      Object[][] routingTable = new Object[10][2]; // routing table
      
      ServerSocket serverSocket = null; // server socket for accepting connections
      try {
         serverSocket = new ServerSocket(5556);
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

      boolean onPi = false;
      try {
         if (InetAddress.getLocalHost().getHostName().equals("turnerpi4")) {
            onPi = true;
         }
      } catch (UnknownHostException e) {
         System.err.println("Could not get local host name.");
         System.exit(1);
      }

      RegisterWithOtherRouter registerWithOtherRouter = new RegisterWithOtherRouter(onPi ? "turners-pc.local" : "turnerpi4.local");
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

class RegisterWithOtherRouter extends Thread {
   private String otherRouterIP;
   
   RegisterWithOtherRouter(String ip) {
      otherRouterIP = ip;
   }

   @Override
   public void run() {
      try {
         Thread.sleep(5000);
         Socket socket = new Socket(otherRouterIP, 5556);
         PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
         writer.println("registerRouter");
         writer.println(InetAddress.getLocalHost().getHostAddress());
         socket.close();
      } catch (IOException | InterruptedException e) {
         System.err.println("Could not connect to Router.");
         System.exit(1);
      }
   }
}