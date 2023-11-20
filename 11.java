import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
public class SocketClient {
    public static void main(String[] args) {
        String serverAddress = "82.179.140.18"; // IP-адрес сервера
        int serverPort = 45126; // Порт сервера

        try (Socket socket = new Socket(serverAddress, serverPort)) {
            // Создаем потоки для отправки и получения данных
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));


            // Читаем ответ от сервера
            String response = in.readLine();
            System.out.println("Ответ от сервера: " + response);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.print(e);
        }
    }
}
