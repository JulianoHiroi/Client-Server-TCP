package main.java.datagram;

import java.net.DatagramPacket;
import java.net.InetAddress;

class DatagramPackegeTCP {
    private DatagramPacket packet;
    private String hostSource;
    private int portSource;
    private byte[] data;
    private int checksum;
    private int dataLength;
    private int ack;
    private boolean syn;
    private boolean fin;
    private boolean ackFlag;
    private int rcvWindow;

    // Essa vai ser o construtor que irá receber um pacote e coletar as informações
    public DatagramPackegeTCP(DatagramPacket packet) {
        this.packet = packet;
        this.data = packet.getData();
        this.dataLength = packet.getLength();
        this.hostSource = packet.getAddress().getHostAddress();
        this.portSource = packet.getPort();
        collectInformation();
    }

    // Esse será o construtor que irá receber os dados e montar o pacote
    public DatagramPackegeTCP(byte[] data, int dataLength) {

    }

    public void collectInformation() {
        this.ack = (int) data[0];
        this.checksum = (int) data[1];
    }

    public int getChecksumFromPacket() {
        return this.checksum;
    }

}