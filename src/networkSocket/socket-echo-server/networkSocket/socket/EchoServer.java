import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;


public class EchoServer {
    public static void echoHandler(Socket socket) {
        if (socket == null) {
            throw new IllegalArgumentException();
        }

        // 소켓에서 InputStream/OutputStream을 얻어내 수신 및 송신에 적용합니다
        try (Scanner socketIn = new Scanner(socket.getInputStream());
             PrintStream socketOut = new PrintStream(socket.getOutputStream())) {

            while (!Thread.currentThread().isInterrupted()) {
                System.out.println("메시지를 기다립니다.");

                // nextLine을 이용해 문자열을 받습니다.
                String line = socketIn.nextLine();

                if (line.isEmpty()) {
                    System.out.println("빈 메시지를 받았습니다. 연결을 끊습니다.");
                    break;
                }
                System.out.print("메시지를 받았습니다: ");
                System.out.println(line);

                // 받은 문자열은 돌려 보냅니다.
                System.out.println("메시지를 그대로 되돌려 보냈습니다.");

                // PrintStream의 print()는 데이터를 출력하는 메소드이고
                //               append()는 데이터를 추가하는 메소드
                // append()는 문자열을 더하는 용도로 주로 사용되며, 
                // StringBuilder나 StringBuffer와 유사하게 동작할 수 있습니다.
                // socketOut.append(line);
                socketOut.println(line);
            }

        } catch (NoSuchElementException e) {
            System.err.println("클라이언트에서 연결을 끊었습니다.");
        } catch (IOException e) {
            System.err.println("데이터 수신중 오류가 발생하였습니다: " + e.getMessage());
        }
        System.out.println("프로그램을 종료합니다.");
    }

    public static void main(String[] args) {
        int port = 12345;

        // 서버 소켓을 생성합니다.
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("소켓을 생성하였습니다. 클라이언트 연결을 기다립니다.");
            
            // 서버 소켓 객체의 accept() 메소드를 이용해 접속을 기다리며, 
            // 클라이언트 접속시 반환되는 소켓을 이용합니다.
            try (Socket socket = serverSocket.accept()) {
                System.out.printf("클라이언트가 연결되었습니다.%n", socket.getInetAddress().getHostAddress(), socket.getPort());
                echoHandler(socket);
            }
        } catch (IOException e) {
            System.err.println("연결에 오류가 발생하였습니다: " + e.getMessage());
        }
    }
}

