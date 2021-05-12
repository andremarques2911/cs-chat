package server;

public class Server {
    public static void main(String args[])  throws Exception {
        try {
            ServerManager sm = new ServerManager();
            sm.executeServer();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
