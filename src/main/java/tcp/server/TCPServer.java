package tcp.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import tcp.packet.*;

public class TCPServer {
    private DatagramSocket socket;
    private InetAddress address;
    private int windowSize;
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
        try {
            RandomAccessFile raf = new RandomAccessFile("arquivos_envio/" + fileName, "r");
            System.out.println("Arquivo encontrado");
            sendMessage("Arquivo encontrado");
            byte[] buffer = new byte[1024];
            int bytesRead;
            int seqNumber = 0;
            byte[] bufferAck = new byte[100];
            DatagramPacket packetAck = new DatagramPacket(bufferAck, bufferAck.length);
            PacketReceiver pacote;
            boolean finish = false;
            windowSize = 10;
            int lastAck = -1;
            int lastSeq = 100000;
            while (!finish) {
                if(lastAck == lastSeq){
                    break;
                }
                if(seqNumber != lastAck + 1){
                    raf.seek((lastAck + 1) * 1024);
                    seqNumber = lastAck + 1;
                }
                for (int i = 0; i < windowSize; i++) {
                    bytesRead = raf.read(buffer);
                    if (bytesRead == -1) {
                        lastSeq = seqNumber - 1;
                        System.out.println("Last Seq : " + lastSeq);
                        break;
                    }
                    byte[] data = new byte[bytesRead];
                    System.arraycopy(buffer, 0, data, 0, bytesRead);
                    PacketTransmitter packet = new PacketTransmitter(data, 1, 0, seqNumber);
                    DatagramPacket datagramPacket = packet.getPacket();
                    socket.send(datagramPacket);
                    seqNumber++;
                }

                socket.setSoTimeout(1);
                try {
                    while (true) {
                        socket.receive(packetAck);
                        pacote = new PacketReceiver(packetAck);
                        System.out.println("Ack recebido: " + pacote.getAck());
                        if(pacote.getAck() > lastAck){
                            lastAck = pacote.getAck();
                        }
                    }
                } catch (IOException e) {
                    socket.setSoTimeout(0);
                }
            }
            raf.close();
            sendFinishMessage();
        } catch (FileNotFoundException e) {
            sendMessage("Arquivo não encontrado");
            System.err.println("Arquivo não encontrado");
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
