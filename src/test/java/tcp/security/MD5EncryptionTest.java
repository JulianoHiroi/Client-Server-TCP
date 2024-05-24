package tcp.security;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MD5EncryptionTest {

    @Test
    public void testIfPayloadGerateTheSameEncriptation() {
        byte[] payload = "teste".getBytes();
        byte[] byteEncription = MD5Encryption.encryptMD5(payload);
        byte[] byteEncription2 = MD5Encryption.encryptMD5(payload);
        Assertions.assertArrayEquals(byteEncription, byteEncription2);
    }

    @Test
    public void testIfCompareEncryptedBytesReturnTrue() {
        byte[] payload = "teste".getBytes();
        byte[] byteEncription = MD5Encryption.encryptMD5(payload);
        boolean result = MD5Encryption.compareEncryptedBytes(byteEncription, payload);
        Assertions.assertTrue(result);
    }
}
