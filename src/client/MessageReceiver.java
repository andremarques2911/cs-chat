package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MessageReceiver extends Thread {

    private DatagramSocket datagramSocket;
    private InetAddress IPAddress;
    private MulticastReceiver multicastReceiver;

    public MessageReceiver(DatagramSocket datagramSocket, InetAddress IPAddress) {
        this.datagramSocket = datagramSocket;
        this.IPAddress = IPAddress;
        this.multicastReceiver = null;
    }

    public void run() {
        while(true) {
            try {
                byte[] data = new byte[1024];
                DatagramPacket receivedPacket = new DatagramPacket(data, data.length);
                this.datagramSocket.receive(receivedPacket);
                String message = new String(receivedPacket.getData(), 0, receivedPacket.getLength());
                if (message.equals("end")) {
                    this.datagramSocket.close();
                    System.exit(1);
                } else if (message.equals("created")) {
//                    new MulticastReceiver().start();
                } else if (message.startsWith("romm::")) {
                    if (this.multicastReceiver != null) {
                        this.multicastReceiver.stop();
                        this.multicastReceiver = null;
                    }
                    int port = Integer.parseInt(message.split("::")[1]);
                    this.multicastReceiver = new MulticastReceiver(port);
                    this.multicastReceiver.start();
                } else {
                    System.out.println(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

