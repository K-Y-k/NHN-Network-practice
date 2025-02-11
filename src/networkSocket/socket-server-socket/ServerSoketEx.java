import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ServerSoketEx {
    public static void main(String[] args) {
        int port = 12345;

        // 서버 소켓을 생성합니다.
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("소켓을 생성하고, 클라이언트의 연결 요청을 기다립니다.");

            // 서버 소켓 객체의 accept() 메서드를 이용해 클라이언트 접속을 기다립니다.
            try (Socket socket = serverSocket.accept()) {

                // 접속한 클라이언트의 정보를 출력합니다.
                System.out.printf("클라이언트[%s:%d]가 연결되었습니다.", socket.getInetAddress().getHostAddress(), socket.getPort());

                // 접속 후 생성된 소켓에서 InputStream을 얻어 Scanner 객체를 생성합니다.
                try (Scanner socketIn = new Scanner(socket.getInputStream())) {

                    while (!Thread.currentThread().isInterrupted()) {
                        System.out.println("메시지를 기다립니다.");
                        String line = socketIn.nextLine();

                        if (line.isEmpty()) {
                            System.out.println("빈 메시지를 받았습니다. 연결을 종료합니다.");
                            break;
                        }

                        System.out.print("메시지를 받았습니다: ");
                        System.out.println(line);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("에러가 발생했습니다: " + e.getMessage());
        }
    }
}
