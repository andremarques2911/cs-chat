package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class ClientManager {

    private final DatagramSocket clientSocket;
    private final InetAddress IPAddress;
    private final Scanner scanner;

    public ClientManager() throws IOException {
        // declara socket cliente
        this.clientSocket = new DatagramSocket();

        // obtem endereço IP do servidor com o DNS
        this.IPAddress = InetAddress.getByName("localhost");

        // cria o stream do teclado
        this.scanner = new Scanner(System.in);

        //Inicia thread responsável por receber mensagens do server
        new MessageReceiver(this.clientSocket, this.IPAddress).start();
    }

    public void executeClient() throws IOException {
        while (true) {
            // lê uma linha do teclado
            String sentence = this.scanner.nextLine();
            byte[] sendData;
            if (sentence.startsWith("::img")) {
                String[] splitData = sentence.split(" ");
                String path = splitData[1];
                byte[] fileBytes = Files.readAllBytes(Paths.get(path));
                byte[] commandBytes = "::img ".getBytes();
                sendData = new byte[commandBytes.length + fileBytes.length];
                System.arraycopy(commandBytes, 0, sendData, 0, commandBytes.length);
                System.arraycopy(fileBytes, 0, sendData, commandBytes.length, fileBytes.length);
            } else {
                sendData = sentence.getBytes();
            }

            // cria pacote com o dado, o endereço do server e porta do servidor
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9880);

            //envia o pacote
            clientSocket.send(sendPacket);
        }
    }

}
