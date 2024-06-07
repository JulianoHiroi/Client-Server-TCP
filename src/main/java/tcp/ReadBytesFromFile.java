package tcp;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ReadBytesFromFile {
    public static void main(String[] args) {
        String filePath = "arquivos/arquivo.txt";
        try {
            FileInputStream fis = new FileInputStream(filePath);
            FileOutputStream fos = new FileOutputStream("arquivos/arquivo_copia.txt");
            // Quero que leia 64 bytes a cada iteração do loop até o fim do arquivo
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
                // Converte os bytes lidos em uma string
                String chunk = new String(buffer, 0, bytesRead);
                System.out.print(chunk); // Use print para continuar na mesma linha
                System.out.println();
                System.out.println("Estou lendo um chunk");
                System.out.println(); // Pula uma linha
            }
            fos.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}