import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 12345;
        
        final Scanner scanner = new Scanner(System.in);
        Socket socket = null;

        try {
            // 소켓을 생성하면서 서버에 연결합니다.
            socket = new Socket(host, port);

            // 소켓이 생성되면, 연결 정보를 출력할 수 있습니다.
            // getInetAddress(), getPort()로 원격 서버 정보를 확인할 수 있습니다.
            System.out.println("서버 IP: " + socket.getInetAddress());
            System.out.println("서버 포트: " + socket.getPort());

            System.out.println("터미널을 열고, netstat를 이용해 소켓 연결 상태를 확인합니다.");
            System.out.println("엔터를 입력하시면 종료합니다.");
            scanner.nextLine();

        } catch (IOException e) {
            System.err.println("서버 연결 과정에서 오류가 발생하였습니다.");
        } finally {
            scanner.close();
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ignore) {
                }
            }
        }
    }
}