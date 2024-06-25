package tcp.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class WebServerTCP implements Runnable {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;

    public WebServerTCP(int portServer) {
        try {
            this.serverSocket = new ServerSocket(portServer, 50, InetAddress.getLocalHost());
            InetAddress serverAdress = serverSocket.getInetAddress();
            System.out.println(serverAdress.getHostAddress());
            System.out.println("Servidor TCP iniciado na porta 9000...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void acceptConnection() {
        try {
            this.clientSocket = serverSocket.accept();
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
            InetAddress localAddress = InetAddress.getLocalHost();
            System.out.println("Conexão estabelecida com o cliente " + localAddress.getHostAddress() + ":" + clientSocket.getPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void handleRequest() {
        try {
            String requestLine = in.readLine();
            if (requestLine == null || !requestLine.startsWith("GET")) {
                return;
            }
            String[] requestParts = requestLine.split(" ");
            if (requestParts.length != 3) {
                return;
            }
            String filePath = requestParts[1].substring(1);  // Remove the leading "/"
            System.out.println("Request for file: " + filePath);
            sendFile(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                sendNotFoundResponse();
                return;
            }

            byte[] fileContent = new byte[(int) file.length()];
            FileInputStream fis = new FileInputStream(file);
            fis.read(fileContent);
            fis.close();

            sendHttpResponse(fileContent, "text/html");  // Assuming HTML for simplicity
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendHttpResponse(byte[] content, String contentType) {
        out.println("HTTP/1.0 200 OK");
        out.println("Content-Type: " + contentType);
        out.println("Content-Length: " + content.length);
        out.println();
        out.flush();
        try {
            clientSocket.getOutputStream().write(content);
            clientSocket.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendNotFoundResponse() {
        String response = "HTTP/1.0 404 Not Found\r\n\r\n";
        out.print(response);
        out.flush();
    }

    public void run() {
        while (true) {
            acceptConnection();
            handleRequest();
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args) {
        WebServerTCP server = new WebServerTCP(9000);
        new Thread(server).start();
    }
}
