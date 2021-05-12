package server;

import java.net.InetAddress;

public class Client {
    private String name;
    private InetAddress IPAddress;
    private int port;
    private int multicastPort;

    public Client(String name, InetAddress IPAddress, int port, int multicastPort) {
        this.name = name;
        this.IPAddress = IPAddress;
        this.port = port;
        this.multicastPort = multicastPort;
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

    public int getMulticastPort() {
        return multicastPort;
    }

    public void setMulticastPort(int multicastPort) {
        this.multicastPort = multicastPort;
    }
}
