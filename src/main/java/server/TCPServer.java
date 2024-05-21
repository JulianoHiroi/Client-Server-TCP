package main.java.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import main.java.packet.*;

public class TCPServer {
    private DatagramSocket socket;

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

    public void start() {
        while (true) {
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                PacketReceiver pacote = new PacketReceiver(packet);
                String payload = new String(pacote.getPayload());
                System.out.println("Mensagem recebida: " + payload);
                if (pacote.validateChecksum()) {
                    System.out.println("Checksum válido");
                } else {
                    System.out.println("Checksum inválido");
                }
                // pacote.printDataHexadecimal();
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
