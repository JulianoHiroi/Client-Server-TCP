package main.java.packet;

import java.net.DatagramPacket;

import main.java.security.MD5Encryption;

public class PacketTransmitter {

    private byte[] data;
    private int dataLength;
    private byte[] payload;
    private int checksum;
    private int ack;
    private int rcvWindow;
    private int seqNumber;
    private byte[] header = null;
    private DatagramPacket packet = null;

    public PacketTransmitter(byte[] payload, int ack, int rcvWindow, int seqNumber) {
        this.payload = payload;
        this.dataLength = payload.length + 12;
        this.data = new byte[dataLength];
        this.ack = ack;
        this.rcvWindow = rcvWindow;
        this.seqNumber = seqNumber;
        byte[] checksumBytes = MD5Encryption.encryptMD5(payload);
        this.checksum = (checksumBytes[0] & 0xFF) << 8 | (checksumBytes[1] & 0xFF);
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

    public void printDataHexadecimal() {
        for (int i = 0; i < dataLength; i++) {
            System.out.print(String.format("%02X ", data[i]));
        }
        System.out.println();
    }

    public DatagramPacket getPacket() {
        return packet;
    }
}