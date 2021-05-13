package server;

import room.Room;
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
//    private final MulticastPublisher multicastPublisher;
    private final List<Client> clients = new CopyOnWriteArrayList<>();
    private final List<Room> rooms = new CopyOnWriteArrayList<>();
    private final int MAX_BUF = 65000;
    private final static int serverPort = 9880;
    private static int multicastPort = 4446;

    public ServerManager() throws IOException {
        this.serverSocket = new DatagramSocket(this.serverPort);
//        this.multicastPublisher = new MulticastPublisher(this.serverSocket, this.multicastPort);
        this.rooms.add(new Room("Principal", this.serverSocket, this.serverPort, this.multicastPort++));
//        this.servers.add(new ServerRef(this.serverSocket, this.multicastPublisher, this.serverPort));
//        this.serverPort++;
//        multicastPublisher.sendMessage("terminate");
//        new KeepAliveManager(clients, this).start();
//        new KeepAliveReceiver(clients, KEEP_ALIVE_PORT).start();

    }

    public void executeServer() throws IOException, InterruptedException {
        byte[] receiveData = new byte[MAX_BUF];

        while (true) {
            // declara o pacote a ser recebido
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            // recebe o pacote do cliente
            this.serverSocket.receive(receivePacket);

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
                case CREATE_ROOM:
                    this.createRoom(param, receivePacket.getAddress(), receivePacket.getPort());
                    break;
                case CREATE_CLIENT:
                    this.createClient(param, receivePacket.getAddress(), receivePacket.getPort());
                    break;
                case LST_ROOMS:
                    this.listRooms(receivePacket.getAddress(), receivePacket.getPort());
                    break;
                case LST_CLIENTS:
                    this.listClientsOnline(receivePacket.getAddress(), receivePacket.getPort());
                    break;
                case ENTER_ROOM:
                    this.enterRoomByName(param, receivePacket.getAddress(), receivePacket.getPort());
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
                case CURRENT_ROOM:
                    this.showCurrentRoom(receivePacket.getAddress(), receivePacket.getPort());
                    break;
                case IMG:
                    this.sendImageToRoom(receivePacket.getData(), receivePacket.getAddress(), receivePacket.getPort());
                case BLOCK:
                    break;
                case DEFAULT:
                    this.sendDefaultMulticastMessage(message, receivePacket.getAddress(), receivePacket.getPort());
                    break;
            }
        }
    }

    private void createRoom(String name, InetAddress IPAddress, int port) throws IOException {
        if (!this.verifyRoom(name, IPAddress, port)) return;
        this.rooms.add(new Room(name.toLowerCase(), this.serverSocket, this.serverPort, this.multicastPort++));
        this.sendMessage("Sala " + name + " criada com sucesso!", IPAddress, port);
    }

    private void createClient(String name, InetAddress IPAddress, int port) throws IOException, InterruptedException {
        if (!this.verifyClient(name, IPAddress, port)) return;
        Client client = new Client(name.toLowerCase(), IPAddress, port, this.rooms.get(0).getMulticastPort());
        this.clients.add(client);
//        this.sendMessage("created", IPAddress, port);
        this.enterRoomByMulticastPort(client.getMulticastPort(), IPAddress, port);
        this.sendMessage("Servidor [privado]: Cliente registrado com sucesso! Para visualizar os comandos disponíveis digite ::help", IPAddress, port);

        // evnia mensagem de boas vindas para todos
        Thread.sleep(100);
        String message = "Servidor [para todos]: O usuário " + name + " acabou de entrar no chat!";
        this.rooms.get(0).getMulticastPublisher().multicast(message);
    }

    private void listRooms(InetAddress IPAddress, int port) throws IOException {
        StringBuilder sb = new StringBuilder("Servidor [privado]: Salas: [ ");
        for (int i = 0; i < this.rooms.size(); i++) {
            sb.append(this.rooms.get(i).getName()).append(i < this.rooms.size() -1 ? ", " : "");
        }
        sb.append(" ]");
        this.sendMessage(sb.toString(), IPAddress, port);
    }

    private void listClientsOnline(InetAddress IPAddress, int port) throws IOException {
        StringBuilder sb = new StringBuilder("Servidor [privado]: Usuários online: [ ");
        for (int i = 0; i < this.clients.size(); i++) {
            sb.append(this.clients.get(i).getName()).append(i < this.clients.size() -1 ? ", " : "");
        }
        sb.append(" ]");
        this.sendMessage(sb.toString(), IPAddress, port);
    }

    private void enterRoomByMulticastPort(int multicastPort, InetAddress IPAddress, int port) throws IOException {
        Room room = this.getRoomByMulticastPort(multicastPort, IPAddress, port);
        if (room == null) return;
        for (Client client : this.clients) {
            if (client.getPort() == port) {
                client.setMulticastPort(room.getMulticastPort());
            }
        }
        this.sendMessage("romm::"+multicastPort, IPAddress, port);
    }

    private void enterRoomByName(String roomName, InetAddress IPAddress, int port) throws IOException, InterruptedException {
        Room room = this.getRoomByName(roomName, IPAddress, port, true);
        if (room == null) return;
        String clientName = "";
        int oldMulticastPort = 0;
        for (Client cli : this.clients) {
            if (cli.getPort() == port) {
                oldMulticastPort = cli.getMulticastPort();
                cli.setMulticastPort(room.getMulticastPort());
                clientName = cli.getName();
            }
        }
        if (oldMulticastPort > 0) {
            Room oldRoom = this.getRoomByMulticastPort(oldMulticastPort, IPAddress, port);
            if (oldRoom == null) return;
            // evnia mensagem de despedida para todos da sala antiga
            oldRoom.getMulticastPublisher().multicast(clientName + " saiu do servidor. '-' ");
        }
        this.sendMessage("romm::"+room.getMulticastPort(), IPAddress, port);
        // evnia mensagem de boas vindas para todos da sala nova
        Thread.sleep(100);
        String message = "Servidor [para todos]: O usuário " + clientName + " acabou de entrar no chat!";
        room.getMulticastPublisher().multicast(message);

    }

    public void sendPrivateMessage(String message, String receiverName, InetAddress IPAddress, int port) throws IOException {
        Client senderClient = this.getClientByPort(IPAddress, port);
        Client receiverClient = this.getClientByName(receiverName, IPAddress, port, true);
        if (senderClient == null || receiverClient == null) return;
        String messageSender = "Você para " + receiverName + " [privado]: " + message;
        String messageReceiver =  senderClient.getName() + " [privado]: " + message;
        this.sendMessage(messageSender, IPAddress, port);
        this.sendMessage(messageReceiver, receiverClient.getIPAddress(), receiverClient.getPort());
    }

    private void listCommands(InetAddress IPAddress, int port) throws IOException {
        StringBuilder sb = new StringBuilder("LISTA DE COMANDOS: \n\n");
        sb.append("::CREATE_CLIENT [name] – criar  usuário;\n");
        sb.append("::CREATE_ROOM [name] – criar uma room;\n");
        sb.append("::LST_ROOMS – lista os rooms do server;\n");
        sb.append("::ENTER_ROOM [name] – entrar na room;\n");
        sb.append("::PV [receiver] [message] – mandar mensagem privada;\n");
        sb.append("::CURRENT_ROOM – listar room atual;\n");
        sb.append("::HELP – listar os comandos;\n");
        sb.append("::END – encerrar participação do usuário no servidor;\n");
        sb.append("::IMG [path] – enviar imagem;\n");
        this.sendMessage(sb.toString(), IPAddress, port);
    }

    private void endClientConnection(InetAddress IPAddress, int port) throws IOException {
        Client client = this.getClientByPort(IPAddress, port);
        Room room = this.getRoomByMulticastPort(client.getMulticastPort(), client.getIPAddress(), client.getPort());
        if (room == null) return;
        this.clients.removeIf(cli -> cli.getPort() == port);
        room.getMulticastPublisher().multicast(client.getName() + " saiu do servidor. '-' ");
        this.sendMessage("end", client.getIPAddress(), client.getPort());
    }

    private void showCurrentRoom(InetAddress IPAddress, int port) throws IOException {
        Client client = this.getClientByPort(IPAddress, port);
        if (client == null) return;
        Room room = this.getRoomByMulticastPort(client.getMulticastPort(), IPAddress, port);
        if (room == null) return;
        this.sendMessage("Servidor [privado]: Sua sala atual é " + room.getName(), IPAddress, port);
    }

    private void sendImageToRoom(byte[] data, InetAddress IPAddress, int port) throws IOException {
        Client client = this.getClientByPort(IPAddress, port);
        if (client == null) return;
        Room room = this.getRoomByMulticastPort(client.getMulticastPort(), client.getIPAddress(), client.getPort());
        if (room == null) return;
        room.getMulticastPublisher().multicast(data);
    }

    private void sendDefaultMulticastMessage(String message, InetAddress IPAddress, int port) throws IOException {
        Client client = this.getClientByPort(IPAddress, port);
        if (client == null) return;
        Room room = this.getRoomByMulticastPort(client.getMulticastPort(), client.getIPAddress(), client.getPort());
        if (room == null) return;
        room.getMulticastPublisher().multicast(client.getName() + ": " + message);
    }

    private void sendMessage(String message, InetAddress IPAddress, int port) throws IOException {
        var buffer = message.getBytes();
        DatagramPacket datagram = new DatagramPacket(buffer, buffer.length, IPAddress, port);
        this.serverSocket.send(datagram);
    }

    private boolean verifyRoom(String name, InetAddress IPAddress, int port) throws IOException {
        if (this.isEmpty(name, IPAddress, port, "Nome não informado! Por favor informe um nome para cadastrar uma sala.")) return false;
        if (this.roomIsRegistered(name, IPAddress, port)) return false;
        return true;
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

    private boolean roomIsRegistered(String name, InetAddress IPAddress, int port) throws IOException {
        Room room = this.getRoomByName(name, IPAddress, port, false);
        if (room != null) {
            this.sendMessage("Servidor [privado]: Já existe uma sala cadastrada com este nome, por favor utilize um outro nome.", IPAddress, port);
            return true;
        }
        return false;
    }

    private boolean clientIsRegistered(String name, InetAddress IPAddress, int port) throws IOException {
        Client client = this.getClientByName(name, IPAddress, port, false);
        if (client != null) {
            this.sendMessage("Servidor [privado]: Já existe um cliente cadastrado com este nome, por favor utilize um outro nome.", IPAddress, port);
            return true;
        }
        return false;
    }

    private Room getRoomByMulticastPort(int multicastPort, InetAddress IPAddress, int port) throws IOException {
        Optional<Room> roomOpt = this.rooms.stream().filter(room -> room.getMulticastPort() == multicastPort).findFirst();
        if (roomOpt.isEmpty()) {
            this.sendMessage("Servidor [privado]: Sala  não encontrada!", IPAddress, port);
            return null;
        }
        Room room = roomOpt.get();
        return room;
    }

    private Room getRoomByName(String name, InetAddress IPAddress, int port, boolean showWarning) throws IOException {
        Optional<Room> roomOpt = this.rooms.stream().filter(room -> room.getName().equals(name.toLowerCase())).findFirst();
        if (roomOpt.isEmpty()) {
            if (showWarning) this.sendMessage("Servidor [privado]: Sala  não encontrada!", IPAddress, port);
            return null;
        }
        Room room = roomOpt.get();
        return room;
    }

    private Client getClientByPort(InetAddress IPAddress, int port) throws IOException {
        Optional<Client> clientOpt = this.clients.stream().filter(cli -> cli.getPort() == port).findFirst();
        if (clientOpt.isEmpty()) {
            this.sendMessage("Servidor [privado]: Cliente não encontrado!", IPAddress, port);
            return null;
        }
        Client client = clientOpt.get();
        return client;
    }

    private Client getClientByName(String name, InetAddress IPAddress, int port, boolean showWarning) throws IOException {
        Optional<Client> clientOpt = this.clients.stream().filter(cli -> cli.getName().equals(name.toLowerCase())).findFirst();
        if (clientOpt.isEmpty()) {
            if (showWarning) this.sendMessage("Servidor [privado]: Cliente não encontrado!", IPAddress, port);
            return null;
        }
        Client client = clientOpt.get();
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
