package tcp;

import java.util.ArrayList;
import java.util.List;

import tcp.client.TCPClient;
import tcp.security.MD5Encryption;
import tcp.server.TCPServer;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        System.out.println(MD5Encryption.encryptMD5File("arquivos_envio/arquivo.txt"));
        System.out.println(MD5Encryption.encryptMD5File("arquivos_recebidos/13206/arquivo.txt"));
    }

}
