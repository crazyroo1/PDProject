import java.io.*;
import java.net.*;

public class RegisterWithOtherRouter extends Thread {
    private String otherRouterIP;
    
    RegisterWithOtherRouter(String ip) {
       otherRouterIP = ip;
    }
 
    @Override
    public void run() {
       try {
          Thread.sleep(5000);
          Socket socket = new Socket(otherRouterIP, Configuration.shared.routerPort);
          PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
          writer.println("registerRouter");
          socket.close();
       } catch (IOException | InterruptedException e) {
          System.err.println("Could not connect to Router.");
          System.exit(1);
       }
    }
 }
