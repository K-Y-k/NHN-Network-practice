import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketConstruct {
    /**
     * Socket 객체 생성 방식들
     */

    public static void main(String[] args) throws IOException {
        /**
         * 1.서버와 연결하는 일반적인 생성자 방식
         * - 서버에 정상적으로 연결되면 Socket 객체가 반환되며, 
         * - 이후 InputStream, OutputStream을 통해 데이터 송수신 가능
         * - "localhost" → 서버의 IP 주소 또는 도메인
         * -  12345      → 서버의 포트 번호
         */
        Socket socket1 = new Socket("localhost", 12345);


        /**
         * 2.InetAddress를 이용한 생성 방식
         * - InetAddress.getByName()을 사용하여 
         *   도메인/IP 주소를 InetAddress 객체로 변환 후 소켓 생성
         */
        InetAddress address = InetAddress.getByName("127.0.0.1");
        Socket socket2 = new Socket(address, 12345);


        /**
         * 3.특정 네트워크 인터페이스에서 연결 방식
         * - example.com의 80번 포트로 연결을 시도하지만,
         *   출발지(로컬 네트워크 인터페이스)를 특정할 수 있음
         * - 50000은 클라이언트가 사용할 로컬 포트 번호
         */
        InetAddress localAddr = InetAddress.getByName("192.168.1.10");
        Socket socket3 = new Socket("example.com", 80, localAddr, 50000);


        /**
         * 타임아웃을 설정한 소켓 연결 방식
         * - connect() 메서드를 사용하여 타임아웃(5000ms) 내에 연결을 시도
         * - 시간이 초과되면 SocketTimeoutException 발생
         */
        Socket socket4 = new Socket();
        socket4.connect(new InetSocketAddress("example.com", 80), 5000);

        /**
         * Socket 주요 메소드
         */
        // 1.서버 및 클라이언트 정보 조회 메소드
        System.out.println("서버 IP: " + socket1.getInetAddress());
        System.out.println("서버 포트: " + socket2.getPort());
        System.out.println("내 IP: " + socket3.getLocalAddress());
        System.out.println("내 포트: " + socket4.getLocalPort());

        // 2.데이터 송수신 메소드
        // - Socket은 내부적으로 InputStream과 OutputStream을 제공하여 데이터를 송수신할 수 있다.
        //
        // 2-1.서버로 데이터 보내기(OutputStream)
        // - PrintWriter를 사용하면 println()을 통해 손쉽게 문자열을 보낼 수 있다.
        // - true → 자동으로 flush() 호출 (버퍼 비우기)
        PrintWriter out = new PrintWriter(socket1.getOutputStream(), true);
        out.println("Hello, Server!");

        // 2-2.서버로부터 데이터 받기 (InputStream)
        // - BufferedReader를 사용하여 문자열을 읽음
        // - readLine()은 한 줄 단위로 데이터를 읽음
        BufferedReader in = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
        String response = in.readLine();
        System.out.println("서버 응답: " + response);
        

        // 3.소켓 닫기 메소드
        // - 소켓을 사용한 후에는 반드시 자원을 해제해야 한다.
        // - close()를 호출하면 연결이 종료되며, 데이터 송수신이 불가능하다.
        socket1.close();

        // 4.try-with-resources 
        // - close()를 명시적으로 호출하지 않아도 
        //   자동으로 자원이 해제됨
        try (Socket socket = new Socket("localhost", 12345);
            PrintWriter out2 = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in2 = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out2.println("Hello, Server!");
            String response1 = in2.readLine();
            System.out.println("서버 응답: " + response1);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
