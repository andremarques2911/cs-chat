package server;

import utils.Commands;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerManager {
    private final DatagramSocket serverSocket;
    private final MulticastPublisher multicastPublisher;
    private final List<ClientRef> clients = new CopyOnWriteArrayList<>();
//    private static List<ServerRef> servers = new CopyOnWriteArrayList<>();
    private static int serverPort = 9880;

    public ServerManager() throws IOException {
        this.serverSocket = new DatagramSocket(this.serverPort);
        this.multicastPublisher = new MulticastPublisher(this.serverSocket);
//        this.servers.add(new ServerRef(this.serverSocket, this.multicastPublisher, this.serverPort));
//        this.serverPort++;
//        multicastPublisher.sendMessage("terminate");
//        new KeepAliveManager(clients, this).start();
//        new KeepAliveReceiver(clients, KEEP_ALIVE_PORT).start();

    }

    public void executeServer() throws IOException {
        byte[] receiveData = new byte[1024];

        while (true) {
            // declara o pacote a ser recebido
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            // recebe o pacote do cliente
            serverSocket.receive(receivePacket);

            // pega mensagem enviada pelo cliente
            String sentence = new String(receivePacket.getData(), 0, receivePacket.getLength());
            String[] splitData = sentence.split(" ");
            Commands command = this.getCommand(splitData);
            String message = this.getMessage(splitData, (command!=null && !command.equals(Commands.DEFAULT)));
            String param = this.getParam(splitData);
            if (command == null) {
                this.sendMessage("Servidor [privado]: Comando não encontrado!", receivePacket.getAddress(), receivePacket.getPort());
                continue;
            }
            switch (command) {
                case CREATE_SERVER:
                    this.createServer();
                    break;
                case CREATE_CLIENT:
                    this.createClient(param, receivePacket.getAddress(), receivePacket.getPort());
                    break;
                case LST_SERVERS:
                    break;
                case LST_CLIENTS:
                    this.listClientsOnline(receivePacket.getAddress(), receivePacket.getPort());
                    break;
                case ENTER_SERVER:
                    break;
                case PV:
                    this.sendPrivateMessage(message, param, receivePacket.getAddress(), receivePacket.getPort());
                    break;
                case HELP:
                    this.listCommands(receivePacket.getAddress(), receivePacket.getPort());
                    break;
                case END:
                    this.endClientConnection(receivePacket.getAddress(), receivePacket.getPort());
                    break;
                case BLOCK:
                    break;
                case DEFAULT:
                    this.sendDefaultMulticastMessage(message, receivePacket.getAddress(), receivePacket.getPort());
                    break;
            }
        }
    }

    private void createServer() throws IOException {
//        ServerManager sm = new ServerManager();
//        sm.executeServer();
    }

    private void createClient(String name, InetAddress IPAddress, int port) throws IOException {
        if (!this.verifyClient(name, IPAddress, port)) return;
        ClientRef client = new ClientRef(name.toLowerCase(), IPAddress, port);
        this.clients.add(client);
        this.sendMessage("registered", IPAddress, port);
        this.sendMessage("Servidor [privado]: Cliente registrado com sucesso! Para visualizar os comandos disponíveis digite ::help", IPAddress, port);

        // evnia mensagem de boas vindas para todos
        String message = "Servidor [para todos]: O usuário " + name + " acabou de entrar no chat!";
        this.multicastPublisher.multicast(message);
    }

    private void listClientsOnline(InetAddress IPAddress, int port) throws IOException {
        StringBuilder sb = new StringBuilder("Servidor [privado]:\nUsuários online: [ ");
        for (int i = 0; i < this.clients.size(); i++) {
            sb.append(this.clients.get(i).getName()).append(i < this.clients.size() -1 ? ", " : "");
        }
        sb.append(" ]");
        this.sendMessage(sb.toString(), IPAddress, port);
    }

    public void sendPrivateMessage(String message, String receiverName, InetAddress IPAddress, int port) throws IOException {
        ClientRef senderClient = this.getClientByPort(IPAddress, port);
        ClientRef receiverClient = this.getClientByName(receiverName, IPAddress, port, true);
        if (senderClient == null || receiverClient == null) return;
        String messageSender = "Você para " + receiverName + " [privado]: " + message;
        String messageReceiver =  senderClient.getName() + " [privado]: " + message;
        this.sendMessage(messageSender, IPAddress, port);
        this.sendMessage(messageReceiver, receiverClient.getIPAddress(), receiverClient.getPort());
    }

    private void listCommands(InetAddress IPAddress, int port) throws IOException {
        this.sendMessage(Arrays.toString(Commands.values()), IPAddress, port);
    }

    private void endClientConnection(InetAddress IPAddress, int port) throws IOException {
        ClientRef client = this.getClientByPort(IPAddress, port);
        this.clients.removeIf(cli -> cli.getPort() == port);
        multicastPublisher.multicast(client.getName() + " saiu do servidor. '-' ");
        this.sendMessage("end", client.getIPAddress(), client.getPort());
    }

    private void sendDefaultMulticastMessage(String message, InetAddress IPAddress, int port) throws IOException {
        ClientRef client = this.getClientByPort(IPAddress, port);
        if (client == null) return;
        this.multicastPublisher.multicast(client.getName() + ": " + message);
    }

    private void sendMessage(String message, InetAddress IPAddress, int port) throws IOException {
        var buffer = message.getBytes();
        DatagramPacket datagram = new DatagramPacket(buffer, buffer.length, IPAddress, port);
        serverSocket.send(datagram);
    }

    private boolean verifyClient(String name, InetAddress IPAddress, int port) throws IOException {
        if (this.isEmpty(name, IPAddress, port, "Nome não informado! Por favor informe um nome para cadastrar um cliente.")) return false;
        if (this.clientIsRegistered(name, IPAddress, port)) return false;
        return true;
    }

    private boolean isEmpty(String name, InetAddress IPAddress, int port, String messageError) throws IOException {
        if (name == null || name.isEmpty()) {
            this.sendMessage("Servidor [privado]: " + messageError, IPAddress, port);
            return true;
        }
        return false;
    }

    private boolean clientIsRegistered(String name, InetAddress IPAddress, int port) throws IOException {
        ClientRef client = this.getClientByName(name, IPAddress, port, false);
        if (client != null) {
            this.sendMessage("Servidor [privado]: Já existe um cliente cadastrado com este nome, por favor utilize um outro nome.", IPAddress, port);
            return true;
        }
        return false;
    }

    private ClientRef getClientByPort(InetAddress IPAddress, int port) throws IOException {
        Optional<ClientRef> clientOpt = this.clients.stream().filter(cli -> cli.getPort() == port).findFirst();
        if (clientOpt.isEmpty()) {
            this.sendMessage("Servidor [privado]: Cliente não encontrado!", IPAddress, port);
            return null;
        }
        ClientRef client = clientOpt.get();
        return client;
    }

    private ClientRef getClientByName(String name, InetAddress IPAddress, int port, boolean showWarning) throws IOException {
        Optional<ClientRef> clientOpt = this.clients.stream().filter(cli -> cli.getName().equals(name.toLowerCase())).findFirst();
        if (clientOpt.isEmpty()) {
            if (showWarning) this.sendMessage("Servidor [privado]: Cliente não encontrado!", IPAddress, port);
            return null;
        }
        ClientRef client = clientOpt.get();
        return client;
    }

    private String getMessage(String[] splitData, boolean isCommand) {
        StringBuilder sb = new StringBuilder();
        for (int i = isCommand ? 2 : 0; i < splitData.length; i++) {
            sb.append(splitData[i]);
            if (i + 1 != splitData.length) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private Commands getCommand(String[] splitData) {
        Commands command = Commands.DEFAULT;
        if (splitData[0].startsWith("::")) {
            command = Commands.valueOfAbbreviation(splitData[0].trim().toUpperCase());
        }
        return command;
    }

    private String getParam(String[] splitData) {
        String param = "";
        if (splitData.length > 1) {
            param = splitData[1];
        }
        return param;
    }
}
