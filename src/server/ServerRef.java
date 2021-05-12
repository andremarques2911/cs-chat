package server;

import java.net.DatagramSocket;

public class ServerRef {
    private DatagramSocket socket;
    private MulticastPublisher multicastPublisher;
    private int port;

    public ServerRef(DatagramSocket socket, MulticastPublisher multicastPublisher, int port) {
        this.socket = socket;
        this.multicastPublisher = multicastPublisher;
        this.port = port;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    public void setSocket(DatagramSocket socket) {
        this.socket = socket;
    }

    public MulticastPublisher getMulticastPublisher() {
        return multicastPublisher;
    }

    public void setMulticastPublisher(MulticastPublisher multicastPublisher) {
        this.multicastPublisher = multicastPublisher;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
