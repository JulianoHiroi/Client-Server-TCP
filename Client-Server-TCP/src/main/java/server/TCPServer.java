package main.java.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

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

    public static void main(String[] args) {
        TCPServer server = new TCPServer(12345);
        DatagramSocket socket = server.getSocket();
        socket.connect(socket.getInetAddress(), 12345);
        while (true) {
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                System.out.println(" sada " + packet.getData());
                System.out.println(packet.getAddress().getHostAddress() + " enviou uma mensagem...");
                String mensagemRecebida = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Mensagem recebida: " + mensagemRecebida);
                if (mensagemRecebida.equals("sair")) {
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

}
