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

    public WebServerTCP(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }
    
    public WebServerTCP(ServerSocket serverSocket, Socket clientSocket) {
        this.serverSocket = serverSocket;
        this.clientSocket = clientSocket;
    }

    public void acceptConnection() {
        try {
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
            System.out.println();
            System.out.println();
            System.out.println("Request: " + requestLine);
            if (requestLine == null || !requestLine.startsWith("GET")) {
                return;
            }
            String[] requestParts = requestLine.split(" ");
            if (requestParts.length != 3) {
                return;
            }
            if (!requestParts[0].equals("GET") || !requestParts[2].equals("HTTP/1.1")) {
                return;
            }
            String filePath = requestParts[1].substring(1);  // Remove the leading "/"
            System.out.println("Request for file: " + filePath);
            if (filePath.isEmpty()) {
                filePath = "index.html";
            }
            sendFile(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendFile(String filePath) {
        try {
            File file = new File("arquivos_envio/" + filePath);
            if (!file.exists()) {
                sendNotFoundResponse();
                return;
            }

            byte[] fileContent = new byte[(int) file.length()];
            try (FileInputStream fis = new FileInputStream(file)) {
                fis.read(fileContent);
            }

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

    public void start() {
        while (true) {
            try {
                Socket client = serverSocket.accept();
                new Thread(new WebServerTCP(serverSocket, client)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void run() {
        try {
            acceptConnection();
            handleRequest();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(9000, 50, InetAddress.getLocalHost());
            WebServerTCP server = new WebServerTCP(serverSocket);
            InetAddress serverAddress = serverSocket.getInetAddress();
            System.out.println(serverAddress.getHostAddress());
            System.out.println("Servidor TCP iniciado na porta 9000...");
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
