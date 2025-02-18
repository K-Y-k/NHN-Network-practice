import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServerExample {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(9999);
        System.out.println("서버 시작: http://localhost:9999");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream writer = clientSocket.getOutputStream();

            // HTTP 응답 성공 전송
            String successResponse = "HTTP/1.1 200 OK\r\nContent-Type: test/html\r\n\r\n" +
                "<html><body><h1>Success!</h1></body></html>";
            
            // HTTP 응답 실패 전송
            String notFoundResponse = "HTTP/1.1 404 Not Found\r\nContent-Type: text/html\r\n\r\n" +
                "<html><body><h1>404 - 페이지를 찾을 수 없습니다.</h1></body></html>";        

            writer.write(successResponse.getBytes());
            writer.flush();
            clientSocket.close();
        }
    }
}
