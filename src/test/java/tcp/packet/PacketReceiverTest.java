package tcp.packet;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import tcp.security.MD5Encryption;

@ExtendWith(MockitoExtension.class)
public class PacketReceiverTest {

    @Test
    void testIfCreatePacketReceiver() throws UnknownHostException {
        MockedStatic<MD5Encryption> md5Encryption = Mockito.mockStatic(MD5Encryption.class);
        byte[] data = new byte[1024];
        for (int i = 0; i < 10; i++) {
            data[i] = (byte) 0;
        }
        byte[] payload = "Teste".getBytes();
        byte[] retornoChecksum = new byte[] { 0x00, 0x0C };

        // Configurar o comportamento do mock md5Encryption
        md5Encryption.when(() -> MD5Encryption.encryptMD5(payload)).thenReturn(retornoChecksum);

        System.arraycopy(retornoChecksum, 0, data, 10, retornoChecksum.length);
        System.arraycopy(payload, 0, data, 12, payload.length);

        InetAddress serverAddress = InetAddress.getByName("localhost");
        int serverPort = 12345;
        DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, serverPort);

        // Use a instância de packetReceiver injetada
        PacketReceiver packetReceiver = new PacketReceiver(packet);

        Assertions.assertEquals(0, packetReceiver.getSeqNumber());
        Assertions.assertEquals(0, packetReceiver.getAck());
        Assertions.assertEquals(0, packetReceiver.getRcvWindow());
        Assertions.assertEquals(12, packetReceiver.getChecksum());
    }
}
