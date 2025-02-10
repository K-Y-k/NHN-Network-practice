import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSocketEx {
    public static void main(String[] args) throws IOException {
        /**
         * - ServerSocket은 TCP/IP 기반의 서버 소켓을 생성하고,
         *   클라이언트의 연결을 대기하는 역할을 한다.
         *
         * - 클라이언트가 접속하면 Socket 객체를 반환하며,
         *   이를 통해 클라이언트와 데이터를 송수신할 수 있다.
         */
        ServerSocket serverSocket = new ServerSocket(8080);

        /**
         * 주요 메소드
         */
        // accept() 메소드
        // - 클라이언트의 연결을 대기하고,
        //   새로운 연결이 발생하면 클라이언트와 통신할 Socket 객체를 반환한다.
        // - accept()는 블로킹 메서드로,
        //   클라이언트가 연결 요청을 보낼 때까지 대기한다.
        Socket clientSocket = serverSocket.accept();
        
        // close() 메소드
        // ServerSocket을 닫아 더 이상 새로운 연결을 수락하지 않도록 한다.
        serverSocket.close();

        // setSoTimeout(int timeout) 메소드
        // - accept() 메서드가 지정된 시간(밀리초) 동안만 대기하도록 설정할 수 있다.
        // - 제한 시간이 지나도 클라이언트가 연결하지 않으면 SocketTimeoutException이 발생한다.
        serverSocket.setSoTimeout(5000); 

        // getLocalPort() 메소드
        // - 현재 ServerSocket이 바인딩된 포트 번호를 반환한다.
        int port = serverSocket.getLocalPort();
    }
}
