package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MulticastPublisher {
    private DatagramSocket socket;
    private InetAddress group;
    private byte[] buf;

    public MulticastPublisher(DatagramSocket socket) {
        this.socket = socket;
    }

    // enviar pacotes para o cliente
    public void multicast(String multicastMessage) throws IOException {
        group = InetAddress.getByName("230.0.0.0");
        buf = multicastMessage.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, group, 4446);
        socket.send(packet);
    }
}