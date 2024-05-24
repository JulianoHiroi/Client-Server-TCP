package tcp.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;

import tcp.packet.*;

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
            String message = "Olá eu sou Juliano";
            byte[] data = message.getBytes();

            // Imprimi os dados em hexadecimal
            PacketTransmitter packet = new PacketTransmitter(data, 0, 0, 0);
            packet.buildPacket();
            DatagramPacket datagramPacket = packet.getPacket();
            datagramPacket.setAddress(serverAddress);
            datagramPacket.setPort(serverPort);
            socket.send(datagramPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        try {
            byte[] payload = message.getBytes();
            PacketTransmitter packet = new PacketTransmitter(payload, 0, 0, 0);
            DatagramPacket datagramPacket = packet.getPacket();
            // packet.printDataHexadecimal();
            datagramPacket.setAddress(serverAddress);
            datagramPacket.setPort(serverPort);
            socket.send(datagramPacket);
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
        Scanner input = new Scanner(System.in);
        client.handShake();
        while (true) {
            System.out.println("Digite uma mensagem para enviar ao servidor: ");
            String message = input.nextLine();
            client.sendMessage(message);
            if (message.equals("sair")) {
                break;
            }
        }
        input.close();
        client.closeSocket();

    }
}
