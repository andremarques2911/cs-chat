package server;

import utils.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MulticastPublisher {
    private DatagramSocket socket;
    private int port;
    private InetAddress group;
    private byte[] buf;

    public MulticastPublisher(DatagramSocket socket, int port) {
        this.socket = socket;
        this.port = port;
    }

    // enviar pacotes para o receiver da sala
    public void multicast(String multicastMessage) throws IOException {
        group = InetAddress.getByName("230.0.0.0");
        buf = multicastMessage.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, group, this.port);
        socket.send(packet);
    }

    // envia bytes da imagem para o receiver da sala
    public void multicast(byte[] data) throws IOException {
        group = InetAddress.getByName("230.0.0.0");
        DatagramPacket packet = new DatagramPacket(data, data.length, group, this.port);
        socket.send(packet);
    }

}