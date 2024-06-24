package tcp.client;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import tcp.packet.*;

public class TCPClient {

    private int serverPort;
    private InetAddress serverAddress;
    private DatagramSocket socket;
    private Scanner input = new Scanner(System.in);

    public TCPClient(String serverAddress, int serverPort) {
        try {
            socket = new DatagramSocket();
            requestConnection(socket, serverAddress, serverPort);
            socket.connect(this.serverAddress, this.serverPort);
            System.out.println("Cliente TCP iniciado...");
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void requestConnection(DatagramSocket socket, String serverAddress, int serverPort) {
        System.out.println("Tentando conectar...");
        int tentativas = 0;
        while (tentativas < 20) {
            try {
                socket.setSoTimeout(500);
                PacketTransmitter packet = new PacketTransmitter("Connect".getBytes(), 0, 0, 0);
                DatagramPacket datagramPacket = packet.getPacket();
                datagramPacket.setAddress(InetAddress.getByName(serverAddress));
                datagramPacket.setPort(serverPort);
                socket.send(datagramPacket);
                DatagramPacket receivePacket = new DatagramPacket(new byte[1024], 1024);
                socket.receive(receivePacket);
                this.serverAddress = receivePacket.getAddress();
                this.serverPort = receivePacket.getPort();
                socket.setSoTimeout(0);
                break;
            } catch (SocketTimeoutException e) {
                tentativas++;
                continue;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (tentativas == 20) {
            System.out.println("Não foi possível conectar ao servidor");
            System.exit(0);
        }

    }

    public void sendMessage(String message) {
        try {
            byte[] payload = message.getBytes();
            PacketTransmitter packet = new PacketTransmitter(payload, 0, 0, 0);
            DatagramPacket datagramPacket = packet.getPacket();

            // packet.printDataHexadecimal();
            socket.send(datagramPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendAck(int ack) {
        try {
            byte[] payload = new byte[0];
            PacketTransmitter packet = new PacketTransmitter(payload, ack, 0, 0);
            DatagramPacket datagramPacket = packet.getPacket();
            socket.send(datagramPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void chat() {
        try {
            String mensagem = "";

            while (mensagem != "sair") {
                System.out.println("Digite a mensagem que deseja enviar: ");
                mensagem = input.nextLine();
                sendMessage(mensagem);
                if (mensagem.equals("sair")) {
                    break;
                }
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                PacketReceiver pacote = new PacketReceiver(packet);
                String payload = new String(pacote.getPayload());
                System.out.println("Mensagem recebida do servidor: " + payload);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void requestFile(String[] words) {
        byte[] buffer = new byte[1100];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        try {
            // Envia requisição e verificação do Arquivo
            int fileSize;
            socket.receive(packet);
            PacketReceiver pacote = new PacketReceiver(packet);
            String payload = new String(pacote.getPayload());
            if (payload.equals("Arquivo não encontrado")) {
                System.out.println("Arquivo não encontrado");
                return;
            } else if (payload.contains("Arquivo encontrado")) {
                System.out.println("Arquivo encontrado");
                String[] wordsPayload = payload.split(" ");
                fileSize = Integer.parseInt(wordsPayload[2]) + 1;
                System.out.println("Tamanho do arquivo: " + fileSize + " pacotes de 1024 bytes");
            } else {
                System.out.println("Erro ao receber arquivo");
                return;
            }
            int[] pacotes = new int[fileSize + 1];
            // Cria uma pasta com o nome arquivos_recebidos + porta do cliente
            Files.createDirectories(Paths.get("arquivos_recebidos/" + serverPort));
            // Recebe o arquivo
            RandomAccessFile fos = new RandomAccessFile("arquivos_recebidos/" + serverPort + "/" + words[1], "rw");
            int seqNumber = 0;
            int lastAck = -1;
            socket.setSoTimeout(1000);
            while (true) {
                socket.receive(packet);
                pacote = new PacketReceiver(packet);
                if (pacote.getAck() == -1) {
                    System.out.println("Recebido pacote de fim de transmissão");
                    System.out.println("Arquivo recebido com sucesso");
                    break;
                }
                seqNumber = pacote.getSeqNumber();
                fos.seek(seqNumber * 1024);
                fos.write(pacote.getPayload());

                pacotes[seqNumber] = 1;
                while (pacotes[lastAck + 1] == 1) {
                    lastAck = lastAck + 1;
                }
                sendAck(lastAck);
            }
            fos.close();
        } catch (SocketTimeoutException e) {
            System.out.println("Servidor não responde - Erro na conexão");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void closeSocket() {
        if (socket != null) {
            socket.close();
        }
    }

    public void start() {
        try {
            while (true) {
                System.out.println();
                System.out.println();
                System.out.println();
                System.out.println();
                System.out.println("Digite a opção que quer enviar: ");
                System.out.println("GET <nome do arquivo> - para solicitar um arquivo ao servidor");
                System.out.println("Chat - para enviar uma mensagem ao servidor e receber uma resposta");
                System.out.println("sair - para encerrar a conexão");
                String message = input.nextLine();
                sendMessage(message);
                if (message.equals("sair")) {
                    break;
                } else if (message.equals("Chat")) {
                    chat();
                }
                String[] words = message.split(" ");
                if (words[0].equals("GET") && words.length == 2) {
                    requestFile(words);
                }
            }
        } finally {
            closeSocket();
        }
    }

    public static void main(String[] args) {
        // Scanner input = new Scanner(System.in);
        // System.out.println("Cliente TCP iniciado...");
        // System.out.println("Digite o endereço do servidor: ");
        // String serverAddress = input.nextLine();
        // System.out.println("Digite a porta do servidor: ");
        // int serverPort = input.nextInt();
        // TCPClient client = new TCPClient(serverAddress, serverPort);
        TCPClient client = new TCPClient("localhost", 12345);
        client.start();
    }
}
