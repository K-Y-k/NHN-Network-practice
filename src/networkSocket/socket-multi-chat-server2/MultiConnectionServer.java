import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiConnectionServer {

    public static void main(String[] args) {
        int port = 12345;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("소켓을 생성하였습니다.");
            while (!Thread.currentThread().isInterrupted()) {
                System.out.println("클라이언트 연결을 기다립니다.");
                Socket socket = serverSocket.accept();
                System.out.printf("클라이언트가 연결되었습니다.%n", socket.getInetAddress().getHostAddress(), socket.getPort());
                new ClientHandler(socket).start();
            }
        } catch (IOException e) {
            System.err.println("연결에 오류가 발생하였습니다: " + e.getMessage());
        }
    }
}
