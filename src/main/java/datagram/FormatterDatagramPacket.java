package main.java.datagram;

import java.net.DatagramPacket;

import main.java.security.MD5Encryption;

public class FormatterDatagramPacket {
    private String hostSource;
    private int portSource;
    private byte[] data;
    private int dataLength;
    private byte[] header = null;
    private byte[] payload = null;
    private int checksum;
    private int ack;
    private int rcvWindow;
    private int seqNumber;
    private DatagramPacket packet = null;


    // Esse construtor irá receber um pacote e coletar as informações
    public FormatterDatagramPacket(DatagramPacket packet) {
        this.data = packet.getData();
        this.dataLength = packet.getLength();
        this.hostSource = packet.getAddress().getHostAddress();
        this.portSource = packet.getPort();
        collectInformation();
    }

    // Esse construtor irá receber os dados e montar o pacote
    public FormatterDatagramPacket(byte[] payload , int ack , int rcvWindow , int seqNumber) {
        this.payload = payload;
        this.dataLength = payload.length + 12;
        this.data = new byte[dataLength];
        this.ack = ack;
        this.rcvWindow = rcvWindow;
        this.seqNumber = seqNumber;
        this.checksum = MD5Encryption.encryptMD5(data).hashCode();
        buildPacket();
    }

    public void buildPacket() {
        // monta o pacote
        this.header = new byte[12];
        header[0] = (byte) (seqNumber >> 24);
        header[1] = (byte) (seqNumber >> 16);
        header[2] = (byte) (seqNumber >> 8);
        header[3] = (byte) (seqNumber);
        header[4] = (byte) (ack >> 24);
        header[5] = (byte) (ack >> 16);
        header[6] = (byte) (ack >> 8);
        header[7] = (byte) (ack);
        header[8] = (byte) (rcvWindow >> 8);
        header[9] = (byte) (rcvWindow);
        header[10] = (byte) (checksum >> 8);
        header[11] = (byte) (checksum);
        System.arraycopy(header, 0, data, 0, 12);
        System.arraycopy(payload, 0, data, 12, dataLength - 12);
        packet = new DatagramPacket(data, dataLength);

    }

    public void collectInformation() {
        // os primeiros 4 bytes do pacote são o seqNumber
        seqNumber = ((data[0] & 0xFF) << 24) | 
        ((data[1] & 0xFF) << 16) | 
        ((data[2] & 0xFF) << 8)  | 
        (data[3] & 0xFF);
        // os próximos 4 bytes são o ack
        ack = ((data[4] & 0xFF) << 24) | 
        ((data[5] & 0xFF) << 16) | 
        ((data[6] & 0xFF) << 8)  |
        (data[7] & 0xFF);
        // os próximos 2 bytes são o rcvWindow
        rcvWindow = ((data[8] & 0xFF) << 8) |
        (data[9] & 0xFF);
        // os próximos 2 bytes são o checksum
        checksum = ((data[10] & 0xFF) << 8) |
        (data[11] & 0xFF);
        // o restante dos bytes são o payload
        payload = new byte[dataLength - 12];
        System.arraycopy(data, 12, payload, 0, dataLength - 12);

    }

    public int getChecksumFromPacket() {
        return this.checksum;
    }
    public int getAckFromPacket() {
        return this.ack;
    }
    public int getRcvWindowFromPacket() {
        return this.rcvWindow;
    }
    public int getSeqNumberFromPacket() {
        return this.seqNumber;
    }
    public String getHostSource() {
        return this.hostSource;
    }
    public int getPortSource() {
        return this.portSource;
    }
    public byte[] getData() {
        return this.data;
    }
    public int getDataLength() {
        return this.dataLength;
    }
    public byte[] getHeader() {
        return this.header;
    }
    public byte[] getPayload() {
        return this.payload;
    }
    public DatagramPacket getPacket() {
        return this.packet;
    }


}