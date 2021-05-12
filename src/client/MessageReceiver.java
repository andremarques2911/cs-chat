package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MessageReceiver extends Thread {

    private DatagramSocket datagramSocket;
    private InetAddress IPAddress;

    public MessageReceiver(DatagramSocket datagramSocket, InetAddress IPAddress) {
        this.datagramSocket = datagramSocket;
        this.IPAddress = IPAddress;
    }

    public void run() {
        while(true) {
            try {
                byte[] data = new byte[1024];
                DatagramPacket receivedPacket = new DatagramPacket(data, data.length);
                datagramSocket.receive(receivedPacket);
                String message = new String(receivedPacket.getData(), 0, receivedPacket.getLength());
                switch (message){
                    case "end":
                        datagramSocket.close();
                        System.exit(1);
                        break;
                    case "registered":
                        new MulticastReceiver().start();
                        break;
                    default:
                        System.out.println(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

