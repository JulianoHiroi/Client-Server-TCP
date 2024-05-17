package main.java.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class TCPClient {

    private int serverPort;
    private InetAddress serverAddress;
    private DatagramSocket socket;

    public TCPClient(String serverAddress, int serverPort) {
        try {
            this.serverAddress = InetAddress.getByName(serverAddress);
            this.serverPort = serverPort;
            this.socket = new DatagramSocket();
            System.out.println("Cliente UDP iniciado...");
        } catch (SocketException e) {

            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handShake() {
        try {
            String mensagem = "Olá, servidor!";
            byte[] buffer = mensagem.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, serverPort);
            socket.send(packet);
            System.out.println("Mensagem enviada para o servidor: " + mensagem);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeSocket() {
        if (socket != null) {
            socket.close();
        }
    }

    public static void main(String[] args) {
        TCPClient client = new TCPClient("localhost", 12345);

        client.handShake();
        client.closeSocket();

    }
}
