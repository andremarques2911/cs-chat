package server;

import java.net.InetAddress;

public class ClientRef {
    private String name;
    private InetAddress IPAddress;
    private int port;

    public ClientRef(String name, InetAddress IPAddress, int port) {
        this.name = name;
        this.IPAddress = IPAddress;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InetAddress getIPAddress() {
        return IPAddress;
    }

    public void setIPAddress(InetAddress IPAddress) {
        this.IPAddress = IPAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
