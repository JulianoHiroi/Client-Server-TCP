package tcp;

import java.util.Arrays;

import tcp.security.MD5Encryption;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        byte[] payload = "oi".getBytes();
        for (int i = 0; i < payload.length; i++) {
            System.out.print(String.format("%02X ", payload[i]));
        }
        System.out.println();
        byte[] nemChecksum = MD5Encryption.encryptMD5(payload);
        for (int i = 0; i < nemChecksum.length; i++) {
            System.out.print(String.format("%02X ", nemChecksum[i]));
        }
        System.out.println();
        byte[] nemChecksum2 = MD5Encryption.encryptMD5(payload);
        for (int i = 0; i < nemChecksum2.length; i++) {
            System.out.print(String.format("%02X ", nemChecksum2[i]));
        }
        System.out.println();

        if (Arrays.equals(nemChecksum, nemChecksum2)) {
            System.out.println("Iguais");
        } else {
            System.out.println("Diferentes");
        }

        int checksum = (nemChecksum[0] & 0xFF) << 8 | (nemChecksum[1] & 0xFF);
        System.out.println(checksum);
        int checksum2 = (nemChecksum2[0] & 0xFF) << 8 | (nemChecksum2[1] & 0xFF);
        System.out.println(checksum2);
    }
}
