import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPServer {
    public static void main(String[] args) {
        DatagramSocket socket = null;
        try {
            // Cria um socket UDP na porta 12345
            socket = new DatagramSocket(12345);
            System.out.println("Servidor UDP iniciado na porta 12345...");

            // Aqui você pode adicionar a lógica para receber e processar os datagramas
            // Veja um exemplo simples de recebimento de datagramas UDP
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            String mensagemRecebida = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Mensagem recebida: " + mensagemRecebida);

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
