import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UDPClient {
    public static void main(String[] args) {
        DatagramSocket socket = null;
        try {
            // Cria um socket UDP
            socket = new DatagramSocket();
            System.out.println("Cliente UDP iniciado...");

            // Define o endereço IP e a porta do servidor
            InetAddress serverAddress = InetAddress.getByName("localhost");
            int serverPort = 12345;

            // Envia uma mensagem para o servidor
            String mensagem = "Olá, servidor!";
            byte[] buffer = mensagem.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, serverPort);
            socket.send(packet);
            System.out.println("Mensagem enviada para o servidor: " + mensagem);

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
}
