package room;

import server.MulticastPublisher;

import java.net.DatagramSocket;

public class Room {
    private String name;
    private DatagramSocket socket;
    private int serverPort;
    private int multicastPort;
    private MulticastPublisher multicastPublisher;

    public Room(String name, DatagramSocket socket, int serverPort, int multicastPort) {
        this.name = name;
        this.socket = socket;
        this.serverPort = serverPort;
        this.multicastPort = multicastPort;
        this.multicastPublisher = new MulticastPublisher(socket, multicastPort);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    public void setSocket(DatagramSocket socket) {
        this.socket = socket;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public int getMulticastPort() {
        return multicastPort;
    }

    public void setMulticastPort(int multicastPort) {
        this.multicastPort = multicastPort;
    }

    public MulticastPublisher getMulticastPublisher() {
        return multicastPublisher;
    }

    public void setMulticastPublisher(MulticastPublisher multicastPublisher) {
        this.multicastPublisher = multicastPublisher;
    }
}
