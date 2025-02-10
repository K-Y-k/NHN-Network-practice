import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class ReceiveData {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 12345;

        // 소켓을 try-with-resources로 생성합니다.
        // 수신을 위한 Scanner을 생성합니다. 
        // Scanner에 사용할 InputStream은 소켓에서 얻어옵니다.
        try (Socket socket = new Socket(host, port);
             Scanner socketIn = new Scanner(socket.getInputStream())) {
            
            System.out.println("메시지 수신을 위한 소켓이 연결되었습니다.");

            while (!Thread.currentThread().isInterrupted()) {
                System.out.println("메시지를 기다립니다.");

                // 소켓을 통해 문자열을 받습니다.
                String line = socketIn.nextLine();

                // 빈 문자열이 오면 종료합니다.
                if (line.isEmpty()) {
                    System.out.println("빈 메시지를 수신하여 종료합니다.");
                    break;
                }
                
                System.out.print("메시지를 수신하였습니다: ");
                System.out.println(line);
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}