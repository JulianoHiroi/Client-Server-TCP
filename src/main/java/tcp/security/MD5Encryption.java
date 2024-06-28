package tcp.security;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
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

            // Executa o hash e retorna os primeiros dois bytes do array de bytes resultante
            byte[] fullHash = md.digest();
            byte[] truncatedHash = new byte[2];
            System.arraycopy(fullHash, 0, truncatedHash, 0, 2);
            return truncatedHash;
        } catch (NoSuchAlgorithmException e) {
            // Lança uma exceção se o algoritmo MD5 não for suportado
            throw new RuntimeException("Algoritmo MD5 não suportado", e);
        }
    }

    // Cria um hash com os bytes de um arquivo e recebe o nome do arquivo como
    // parâmetro
    public static String encryptMD5File(String fileName) {
        try {
            // Cria uma instância de MessageDigest com o algoritmo MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // Cria um RandomAccessFile com o nome do arquivo
            RandomAccessFile file = new RandomAccessFile(fileName, "r");

            // Lê os bytes do arquivo
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = file.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }

            // Executa o hash
            byte[] fullHash = md.digest();
            file.close();

            // Converte o hash para uma string hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : fullHash) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // Lança uma exceção se o algoritmo MD5 não for suportado
            throw new RuntimeException("Algoritmo MD5 não suportado", e);
        } catch (FileNotFoundException e) {
            // Lança uma exceção se o arquivo não for encontrado
            throw new RuntimeException("Arquivo não encontrado", e);
        } catch (IOException e) {
            // Lança uma exceção se ocorrer um erro de I/O
            throw new RuntimeException("Erro de I/O", e);
        }
    }

    public static boolean compareEncryptedBytes(byte[] encryptedBytes, byte[] plainBytes) {
        // Encripta os bytes não encriptados
        byte[] newEncryptedBytes = encryptMD5(plainBytes);

        // Compara os arrays de bytes
        return Arrays.equals(encryptedBytes, newEncryptedBytes);
    }

}