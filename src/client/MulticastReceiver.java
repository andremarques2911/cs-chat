package client;

import java.io.File;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MulticastReceiver extends Thread {
    private int port;
    protected MulticastSocket socket = null;
    protected byte[] buf = new byte[20000];

    public MulticastReceiver(int port) {
        this.port = port;
    }

    public void run() {
        try {
            this.socket = new MulticastSocket(this.port);
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
                } else if (received.startsWith("::img")) {
                    String[] splitData = received.split("::img");
                    String image = splitData[1];
                    byte[] fileBytes = image.getBytes();
//                    byte[] fileBytes = new byte[image.length()];
//                    System.arraycopy(image, 0, fileBytes, 0, image.length());

//                    File file = new File("/src/images/"+this.port+"/image.PNG");
//                    if (!file.exists()) {
                        //Creating the directory
//                        boolean bool = file.mkdir();
//                    }
                    Path path = Paths.get("/src/images/"+this.port+"/image.PNG");
                    Files.createDirectories(path.getParent());

                    Files.write(path, fileBytes);
//                    FileOutputStream in = new FileOutputStream(file);
//                    in.write(fileBytes);
//                    in.close();
                    break;
                }
                // exibe mensagens do servidor para todos da sala
                System.out.println(received);
            }
            socket.leaveGroup(group);
            socket.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}