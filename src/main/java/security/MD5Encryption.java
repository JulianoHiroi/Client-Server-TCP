package main.java.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class MD5Encryption {

    public static byte[] encryptMD5(byte[] inputBytes) {
        try {
            // Cria uma instância de MessageDigest com o algoritmo MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // Atualiza o MessageDigest com os bytes de entrada
            md.update(inputBytes);

            // Executa o hash e retorna o array de bytes resultante
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            // Lança uma exceção se o algoritmo MD5 não for suportado
            throw new RuntimeException("Algoritmo MD5 não suportado", e);
        }
    }
    public static boolean compareEncryptedBytes(byte[] encryptedBytes, byte[] plainBytes) {
        // Encripta os bytes não encriptados
        byte[] newEncryptedBytes = encryptMD5(plainBytes);

        // Compara os arrays de bytes
        return Arrays.equals(encryptedBytes, newEncryptedBytes);
    }


}