package tcp;

import tcp.client.TCPClient;
import tcp.server.TCPServer;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        Thread app1Thread = new Thread(() -> {
            TCPClient.main(new String[0]);
        });

        // Inicializa a segunda aplicação
        Thread app2Thread = new Thread(() -> {
            TCPServer.main(new String[0]);
        });
        app1Thread.start();
        app2Thread.start();
        try {
            // Espera ambas as aplicações terminarem
            app1Thread.join();
            app2Thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
