package client;

import utils.Utils;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastReceiver extends Thread {
    private int port;
    private final int MAX_BUF = 65000;
    protected MulticastSocket socket = null;
    protected byte[] buf = new byte[MAX_BUF];


    public MulticastReceiver(int port) {
        this.port = port;
    }

    public void run() {
        try {
            // cria socket para multicast e adiciona o client no grupo de nulticast
            this.socket = new MulticastSocket(this.port);
            InetAddress group = InetAddress.getByName("230.0.0.0");
            socket.joinGroup(group);
            while (true) {
                // recebe o packat do publisher
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                String received = new String(packet.getData(), 0, packet.getLength());
                if ("end".equals(received)) {
                    System.out.println("Servidor interrompeu a conexão.");
                    System.exit(1);
                    break;
                } else if (received.startsWith("::img")) {
                    byte[] data = packet.getData();
                    byte[] fileBytes = new byte[MAX_BUF];
                    int positionA = 0;
                    int positionB = 0;
                    // pega os bytes da imagem que estao logo apos dos bytes do comando "::img "
                    for (byte b : data) {
                        if (positionB > 5) {
                            fileBytes[positionA++] = b;
                        }
                        positionB++;
                    }
                    // cria diretorio para a sala caso nao exista e grava o arquivo de imagem na pasta
                    String dir = "src/images/"+this.port;
                    String fileName = "/image_"+System.currentTimeMillis()+".png";
                    Utils.createFile(fileBytes, dir, fileName);
                    System.out.println("Imagem criada no diretório " + dir);
                    continue;
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