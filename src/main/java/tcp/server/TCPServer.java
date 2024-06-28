package tcp.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Random;
import java.util.Scanner;

import tcp.packet.*;
import tcp.security.MD5Encryption;

public class TCPServer implements Runnable {
    private DatagramSocket socket;
    private InetAddress address;
    private int windowSize;
    private int portClient;
    private Scanner input = new Scanner(System.in);

    public TCPServer(int portServer) {
        try {
            this.socket = new DatagramSocket(portServer);
            System.out.println("Servidor TCP iniciado na porta 12345...");
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public TCPServer(int portServer, InetAddress address, int portClient) {
        acceptConection(portServer, address, portClient);
    }

    public void acceptConection(int portServer, InetAddress address, int portClient) {
        try {
            this.socket = new DatagramSocket(portServer);
            this.address = address;
            this.portClient = portClient;
            socket.connect(address, portClient);
            // envia a mensagem de conexão para o cliente com a nova porta do servidor
            PacketTransmitter packet = new PacketTransmitter((portServer + " Conexão estabelecida").getBytes(), 0, 0,
                    0);
            DatagramPacket datagramPacket = packet.getPacket();
            socket.send(datagramPacket);
            System.out.println("Thread para cliente TCP iniciado na porta " + portServer + "...");
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        try {
            byte[] payload = message.getBytes();
            PacketTransmitter packet = new PacketTransmitter(payload, 0, 0, 0);
            DatagramPacket datagramPacket = packet.getPacket();
            socket.send(datagramPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendFinishMessage(String filePath) {
        String HashFile = MD5Encryption.encryptMD5File(filePath);
        PacketTransmitter packet = new PacketTransmitter(HashFile.getBytes(), -1, 0, 0);
        DatagramPacket datagramPacket = packet.getPacket();
        try {
            socket.send(datagramPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendFile(String fileName) {
        String filePath = "arquivos_envio/" + fileName;
        try {
            RandomAccessFile raf = new RandomAccessFile(filePath, "r");
            System.out.println("Arquivo encontrado");
            sendMessage("Arquivo encontrado " + raf.length() / 1024);

            byte[] buffer = new byte[1024];
            int bytesRead;
            int seqNumber = 0;
            byte[] bufferAck = new byte[100];
            DatagramPacket packetAck = new DatagramPacket(bufferAck, bufferAck.length);
            PacketReceiver pacote;

            windowSize = 100;
            int lastAck = -1;
            long fileSize = raf.length() / 1024;

            while (lastAck != fileSize) {
                raf.seek((lastAck + 1) * 1024);
                seqNumber = lastAck + 1;

                // Envia os pacotes
                for (int i = 0; i < windowSize; i++) {
                    bytesRead = raf.read(buffer);
                    if (bytesRead == -1) {
                        System.out.println("Fim do arquivo");
                        break;
                    }
                    byte[] data = new byte[bytesRead];
                    System.arraycopy(buffer, 0, data, 0, bytesRead);
                    PacketTransmitter packet = new PacketTransmitter(data, 1, 0, seqNumber);
                    DatagramPacket datagramPacket = packet.getPacket();
                    socket.send(datagramPacket);
                    seqNumber++;
                }

                // Lê os acks que chegaram
                int acksReceived = 0;
                socket.setSoTimeout(1);
                try {
                    while (true) {
                        socket.receive(packetAck);
                        acksReceived++;
                        pacote = new PacketReceiver(packetAck);
                        System.out.println("Ack recebido: " + pacote.getAck());
                        if (pacote.getAck() > lastAck) {
                            lastAck = pacote.getAck();
                        }
                    }
                } catch (IOException e) {
                    socket.setSoTimeout(0);
                }
                if (acksReceived == 0) {
                    socket.setSoTimeout(2000);
                    try {
                        socket.receive(packetAck);
                        pacote = new PacketReceiver(packetAck);
                        // System.out.println("Ack recebido: " + pacote.getAck());
                        if (pacote.getAck() > lastAck) {
                            lastAck = pacote.getAck();
                        }
                    } catch (IOException e) {
                        System.out.println("Timeout - Fechando a conexão");
                        socket.setSoTimeout(0);
                        raf.close();
                        return;
                    }
                }
            }
            ;
            raf.close();
            System.out.println("Enviando mensagem de FIM");
            sendFinishMessage(filePath);
        } catch (FileNotFoundException e) {
            sendMessage("Arquivo não encontrado");
            System.err.println("Arquivo não encontrado");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getRandomPort() {
        Random random = new Random();
        int port;
        while (true) {
            port = random.nextInt(20000 - 12345) + 12345;
            if (isPortAvailable(port)) {
                break;
            }
        }
        return port;
    }

    public void chat() {
        try {
            String mensagem = "";
            System.out.println("Chat iniciado com o cliente: " + address + ":" + portClient);
            while (mensagem != "sair") {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                PacketReceiver pacote = new PacketReceiver(packet);
                String payload = new String(pacote.getPayload());
                if (payload.equals("sair")) {
                    System.out.println("Chat encerrado com o cliente: " + address + ":" + portClient);
                    break;
                }
                System.out.println("Mensagem recebida do " + address + ":" + portClient + ": " + payload);
                System.out.println(
                        "Digite a mensagem que deseja enviar para o cliente" + address + ":" + portClient + ":");
                mensagem = input.nextLine();
                sendMessage(mensagem);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void run() {
        while (true) {
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                PacketReceiver pacote = new PacketReceiver(packet);
                String payload = new String(pacote.getPayload());
                System.out.println("Mensagem recebida do " + address + ":" + portClient + ": " + payload);
                String[] words = payload.split(" ");
                // pacote.printDataHexadecimal();
                if (words[0].equalsIgnoreCase("get") && words.length == 2) {
                    sendFile(words[1]);
                } else if (payload.equals("sair")) {
                    socket.close();
                    System.out.println("Conexão encerrada com o cliente: " + address + ":" + portClient);
                    break;
                } else if (payload.equals("Chat")) {
                    chat();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void on() {
        while (true) {
            try {
                System.out.println("O host do servidor é " + InetAddress.getLocalHost().getHostAddress());
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                address = packet.getAddress();
                portClient = packet.getPort();
                int port = getRandomPort();
                new Thread(new TCPServer(port, address, portClient)).start();
                System.out.println("Conexão estabelecida com o cliente " + address + " na porta " + portClient);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        TCPServer server = new TCPServer(12345);
        server.on();
    }
}