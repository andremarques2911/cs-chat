package client;

public class Client {

    public static void main(String[] args) {
        try {
            ClientManager cm = new ClientManager();
            cm.executeClient();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

}
