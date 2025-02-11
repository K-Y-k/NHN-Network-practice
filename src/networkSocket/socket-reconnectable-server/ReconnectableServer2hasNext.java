import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ReconnectableServer {
    public static void echoHandler(Socket socket) {
        // 인수의 유효성의 검사합니다. 유효하지 않을 경우, IllegalArgumentException()을 발생
        if (socket == null) {
            throw new IllegalArgumentException();
        }

        /*
         * 소켓에서 데이터 송수신을 위한 I/O Stream을 가져와 관련 객체를 생성하고,
         * 클라이언트와 데이터를 주고 받습니다.
         */
        try (PrintStream socketOut = new PrintStream(socket.getOutputStream());
             Scanner socketIn = new Scanner(socket.getInputStream())) {
            
            while (true) {
                if (socketIn.hasNext()) {

                    System.out.println("메시지를 기다립니다.");

                    String line = socketIn.nextLine();

                    if (line.isEmpty()) {
                        break;
                    }

                    System.out.print("메시지를 받았습니다: ");
                    System.out.println(line);

                    System.out.println("메시지를 그대로 되돌려 보냈습니다.");
                    socketOut.println(line);
                } else {
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int port = 12345;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("소켓을 생성하였습니다.");

            while (!Thread.currentThread().isInterrupted()) {
                System.out.println("클라이언트 연결을 기다립니다.");
                
                /*
                 * 클라이언트 접속을 기다리며, 접속하여 생성되는 소켓으로 메시지를 주고 받습니다.
                 * 연결이 끊어지면, 다시 접속을 기다립니다.
                 */
                try (Socket socket = serverSocket.accept()) {
                    System.out.printf("클라이언트가 연결되었습니다.%n", socket.getInetAddress().getHostAddress(), socket.getPort());
                    
                    echoHandler(socket);
                }
            }
        } catch (IOException e) {
            System.err.println("연결에 오류가 발생하였습니다: " + e.getMessage());
        }
    }
}
