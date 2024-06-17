package tcp.server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import tcp.packet.*;

public class TCPServer {
    private DatagramSocket socket;
    private InetAddress address;
    private int port;

    public TCPServer(int port) {
        try {
            this.socket = new DatagramSocket(port);
            System.out.println("Servidor UDP iniciado na porta 12345...");
        } catch (SocketException e) {
            e.printStackTrace();
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
    public DatagramSocket getSocket() {
        return socket;
    }

    public void sendFinishMessage() {
        PacketTransmitter packet = new PacketTransmitter("sair".getBytes(), -1, 0, 0);
        DatagramPacket datagramPacket = packet.getPacket();
        try {
            socket.send(datagramPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendFile(String fileName) {
        //Verifica a existência do arquivo e envia uma mensagem de erro caso não exista
        // caso exista, envia que existe o arquivo

        try {
            
            FileInputStream fis = new FileInputStream("arquivos_envio/" + fileName);
            System.out.println("Arquivo encontrado");
            sendMessage("Arquivo encontrado");
            byte[] buffer = new byte[1024];
            int bytesRead; // Lê o primeiro chunk de bytes (1024 bytes)
            int seqNumber = 0;
            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] data = new byte[bytesRead];
                System.arraycopy(buffer, 0, data, 0, bytesRead);
                PacketTransmitter packet = new PacketTransmitter(data, 1, 0, seqNumber);
                DatagramPacket datagramPacket = packet.getPacket();
                socket.send(datagramPacket);
                seqNumber ++ ;
            }
            byte[] bufferAck = new byte[100];
            DatagramPacket packetAck = new DatagramPacket(bufferAck, bufferAck.length);
            PacketReceiver pacote;
            socket.setSoTimeout(1);
            try{
                while (true) {
                    socket.receive(packetAck);
                    pacote = new PacketReceiver(packetAck);
                    System.out.println("Ack recebido: " + pacote.getAck());
                    if (pacote.getAck() == -1) {
                        break;
                    }
                }
            } catch (IOException e) {
                socket.setSoTimeout(0);
            }

            fis.close();
            sendFinishMessage();

        }catch (FileNotFoundException e){
            sendMessage("Arquivo não encontrado");  
            System.err.println("Arquivo não encontrado");
            return;
        
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        while (true) {
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                PacketReceiver pacote = new PacketReceiver(packet);
                address = packet.getAddress();
                port = packet.getPort();
                socket.connect(address, port);
                String payload = new String(pacote.getPayload());
                System.out.println("Mensagem recebida: " + payload);
                String[] words = payload.split(" ");
                // pacote.printDataHexadecimal();
                if (words[0].equalsIgnoreCase("get") && words.length == 2){
                    sendFile(words[1]);
                }
                if (payload.equals("sair")) {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (socket != null) {
            socket.close();
        }
    }

    public static void main(String[] args) {
        TCPServer server = new TCPServer(12345);
        server.start();

    }

}
