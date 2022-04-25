import java.net.InetAddress;

public class Configuration {
    public static Configuration shared = new Configuration();

    public boolean onPi;
    public String otherRouterIP;
    public int routerPort;
    public int clientPort;

    public Configuration() {
        try {
            onPi = InetAddress.getLocalHost().getHostName().equals("turnerpi4");
            otherRouterIP = onPi ? "turners-pc.local" : "turnerpi4.local";
            routerPort = 5556;
            clientPort = 5555;
        } catch (Exception e) {
            System.err.println("Could not get local host name.");
            System.exit(1);
        }
    }
}
