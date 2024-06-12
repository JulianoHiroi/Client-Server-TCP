package tcp.server;

import java.io.FileInputStream;
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
        try {
            FileInputStream fis = new FileInputStream("arquivos/" + fileName);
            byte[] buffer = new byte[1024];
            int bytesRead; // Lê o primeiro chunk de bytes (1024 bytes)
            int seqNumber = 0;
            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] data = new byte[bytesRead];
                seqNumber ++ ;
                System.arraycopy(buffer, 0, data, 0, bytesRead);
                PacketTransmitter packet = new PacketTransmitter(data, 1, 0, seqNumber);
                DatagramPacket datagramPacket = packet.getPacket();
                socket.send(datagramPacket);
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
                e.printStackTrace();
            }

            fis.close();
            sendFinishMessage();

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
                String payload = new String(pacote.getPayload());
                System.out.println("Mensagem recebida: " + payload);
                address = packet.getAddress();
                port = packet.getPort();
                socket.connect(address, port);

                // pacote.printDataHexadecimal();
                if (payload.equals("arquivo.txt")) {
                    sendFile(payload);
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
