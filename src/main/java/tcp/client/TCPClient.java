package tcp.client;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

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

    public void requestFile(String[] words) {
        byte[] buffer = new byte[1100];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length); 
        try {
            // Envia requisição e verificação do Arquivo
            int fileSize;
            socket.receive(packet);
            PacketReceiver pacote = new PacketReceiver(packet);
            String payload = new String(pacote.getPayload());
            if(payload.equals("Arquivo não encontrado")){
                System.out.println("Arquivo não encontrado");
                return;
            }else if(payload.contains("Arquivo encontrado")){
                System.out.println("Arquivo encontrado");
                String[] wordsPayload = payload.split(" ");
                fileSize = Integer.parseInt(wordsPayload[2]) + 1;
                System.out.println("Tamanho do arquivo: " + fileSize + " pacotes de 1024 bytes");
            }else{
                System.out.println("Erro ao receber arquivo");
                return;
            }
            int[] pacotes = new int[fileSize + 1];

            // Recebe o arquivo
            RandomAccessFile fos = new RandomAccessFile("arquivos_recebidos/" + words[1], "rw");
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
                pacotes[seqNumber] = 1;
                while(pacotes[lastAck + 1] == 1 ){
                    lastAck = lastAck + 1;
                }
                sendAck(lastAck);
            }
            fos.close();
        }catch ( SocketTimeoutException e){
            System.out.println("Servidor não responde - Erro na conexão");
            
        }catch (IOException e) {
            e.printStackTrace();
        }
        
    }
        



    public void closeSocket() {
        if (socket != null) {
            socket.close();
        }
    }
    public void start() {
        Scanner input = new Scanner(System.in);
        while (true) {
            System.out.println("Digite uma mensagem para enviar ao servidor: ");
            String message = input.nextLine();
            sendMessage(message);
            if (message.equals("sair")) {
                
                break;
            }
            String[] words = message.split(" ");

            if (words[0].equals("GET") && words.length == 2) {
                requestFile(words);
            }
        }
        input.close();
        closeSocket();
    }
    public static void main(String[] args) {
        TCPClient client = new TCPClient("localhost", 12345);
        client.start();
    }
}
