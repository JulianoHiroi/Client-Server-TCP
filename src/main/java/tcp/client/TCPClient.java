package tcp.client;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import tcp.packet.*;

public class TCPClient {

    private int serverPort;
    private InetAddress serverAddress;
    private DatagramSocket socket;
    private int numberPacketMinimum = 2;

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
        String message = words[0] + " " + words[1]; 
        byte[] buffer = new byte[1050];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length); 
        try {
            // Envia requisição e verificação do Arquivo
            socket.receive(packet);
            PacketReceiver pacote = new PacketReceiver(packet);
            String payload = new String(pacote.getPayload());
            if(payload.equals("Arquivo não encontrado")){
                System.out.println("Arquivo não encontrado");
                return;
            }else if(payload.equals("Arquivo encontrado")){
                System.out.println("Arquivo encontrado");
            }else{
                System.out.println("Erro ao receber arquivo");
                return;
            }


            // Recebe o arquivo
            FileOutputStream fos = new FileOutputStream("arquivos_recebidos/" + words[1]);
            List<PacketReceiver> pacotes = new ArrayList<PacketReceiver>(10);
            for (int i = 0; i < 10; i++) {
                pacotes.add(null);
            }
            int seqInitial = 0;
            int seqNumber = 0;
            while (true) {
                socket.receive(packet);
                pacote = new PacketReceiver(packet);
                if (pacote.getAck() == -1) {
                    System.out.println("Arquivo recebido com sucesso");
                    break;
                }
                System.out.println("Pacote recebido: " + pacote.getSeqNumber());
                seqNumber = pacote.getSeqNumber();
                int posArray = seqNumber - seqInitial;
                if(pacotes.get(posArray) == null){
                    pacotes.set(posArray, pacote);
                }
                int numberAck = seqInitial;
                for (int i = 0; i < pacotes.size(); i++){
                    //Quando tiver 10 pacotes não nulos no array, escreve no arquivo e move o array para 10 posiçõe na
                    //esquerda.
                    
                    if(i == numberPacketMinimum){
                        //System.out.println("Antes de escrever no arquivo");
                        //System.out.println(pacotes);
                        for(int j = 0; j < numberPacketMinimum; j++){
                            fos.write(pacotes.get(j).getPayload());
                        }
                        
                        for (int j = 0; j < numberPacketMinimum; j++) {
                            pacotes.removeFirst();
                            pacotes.add(null);
                        }
                        seqInitial = seqInitial + numberPacketMinimum;
                        i = 0;
                        //System.out.println("Depois de escrever no arquivo");
                        //System.out.println(pacotes);
                    } 
                    
                    if (pacotes.get(i) == null) {
                        System.out.println("Pacote nulo: " + i);
                        numberAck = seqInitial + i - 1;
                        break;
                    }
                }
                sendAck(numberAck);
                
                
            }
            fos.close();
            System.out.println(pacotes.size() + " pacotes recebidos");
            System.out.println(pacotes);
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
