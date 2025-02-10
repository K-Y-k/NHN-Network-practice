import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class SendData {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 12345;

        // Scanner를 이용해 터미널 입력을 받을 객체와 서버와 통신할 소켓을 생성합니다.
        // 소켓과 스캐너 모두 try-with-resources로 생성합니다.
        try (Socket socket = new Socket(host, port);
             Scanner terminalIn = new Scanner(System.in)) {

            System.out.println("메시지 전송을 위한 소켓이 연결되었습니다.");

            // 출력을 위한 PrintStream을 생성합니다.
            // PrintStream에 사용할 OutputStream은 소켓에서 얻어옵니다.
            PrintStream socketOut = new PrintStream(socket.getOutputStream());

            while (!Thread.currentThread().isInterrupted()) {
                System.out.print("메시지를 입력하세요(엔터만 입력하면 종료): ");

                // 터미널에서 스캐너로 전송할 메시지를 입력받습니다.
                String line = terminalIn.nextLine();

                // 입력된 메시지가 없이 엔터만 입력될 경우, 종료합니다.
                if (line.isEmpty()) {
                    break;
                }

                // 입력된 메시지를 전송합니다.
                socketOut.append(line);

                System.out.println("메시지를 전송하였습니다.");
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
