package main.java.packet;

import java.net.DatagramPacket;
import main.java.security.*;

public class PacketReceiver {
    private byte[] data;
    private int dataLength;
    private String hostSource;
    private int portSource;
    private int seqNumber;
    private int ack;
    private int rcvWindow;
    private int checksum;
    private byte[] payload;

    public PacketReceiver(DatagramPacket packet) {
        this.data = packet.getData();
        this.dataLength = packet.getLength();
        this.hostSource = packet.getAddress().getHostAddress();
        this.portSource = packet.getPort();
        collectInformation();
    }

    public void collectInformation() {
        // os primeiros 4 bytes do pacote são o seqNumber
        seqNumber = ((data[0] & 0xFF) << 24) |
                ((data[1] & 0xFF) << 16) |
                ((data[2] & 0xFF) << 8) |
                (data[3] & 0xFF);
        // os próximos 4 bytes são o ack
        ack = ((data[4] & 0xFF) << 24) |
                ((data[5] & 0xFF) << 16) |
                ((data[6] & 0xFF) << 8) |
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

    public boolean validateChecksum() {
        byte[] validateChecksumBytes = MD5Encryption.encryptMD5(payload);
        int validateChecksum = (validateChecksumBytes[0] & 0xFF) << 8 | (validateChecksumBytes[1] & 0xFF);
        if (validateChecksum == checksum) {
            return true;
        }
        return false;
    }

    public void printDataHexadecimal() {
        for (int i = 0; i < dataLength; i++) {
            System.out.print(String.format("%02X ", data[i]));
        }
        System.out.println();
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

    public byte[] getPayload() {
        return this.payload;
    }
}
