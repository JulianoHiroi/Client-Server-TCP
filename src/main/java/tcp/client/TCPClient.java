package tcp.client;

import java.io.FileOutputStream;
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
            socket.connect(this.serverAddress, this.serverPort);
            System.out.println("Cliente UDP iniciado...");
        } catch (SocketException e) {

            e.printStackTrace();
        } catch (Exception e) {
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
    public void sendAck(int ack){
        try{
            byte[] payload = new byte[0];
            PacketTransmitter packet = new PacketTransmitter( payload , ack, 0, 0);
            DatagramPacket datagramPacket = packet.getPacket();
            socket.send(datagramPacket);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void receiveFile() {
        try {
            FileOutputStream fos = new FileOutputStream("arquivos/arquivo_recebido.txt");
            while (true) {
                byte[] buffer = new byte[1050];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                PacketReceiver pacote = new PacketReceiver(packet);
                if (pacote.getAck() == -1) {
                    break;
                }
                sendAck(pacote.getSeqNumber());
                fos.write(pacote.getPayload());

            }
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
        while (true) {
            System.out.println("Digite uma mensagem para enviar ao servidor: ");
            String message = input.nextLine();
            client.sendMessage(message);
            if (message.equals("sair")) {
                break;
            }
            if (message.equals("arquivo.txt")) {
                client.receiveFile();
            }
        }
        input.close();
        client.closeSocket();

    }
}
