package client;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastReceiver extends Thread {
    private int port;
    protected MulticastSocket socket = null;
    protected byte[] buf = new byte[256];

    public MulticastReceiver(int port) {
        this.port = port;
    }

    public void run() {
        try {
            socket = new MulticastSocket(this.port);
            InetAddress group = InetAddress.getByName("230.0.0.0");
            socket.joinGroup(group);
            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                if ("end".equals(received)) {
                    System.out.println("Servidor interrompeu a conexão.");
                    System.exit(1);
                    break;
                }
                // exibe mensagens do servidor para todos
                System.out.println(received);
            }
            socket.leaveGroup(group);
            socket.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}