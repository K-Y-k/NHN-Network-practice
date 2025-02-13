import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
public class MessageServer implements Runnable {

    private static final int DEFAULT_PORT = 8888;
    private final int port;
    private final ServerSocket serverSocket;

    public MessageServer() throws IOException {
        // 기본 생성자를 초기화 합니다. port 지정이 안된다면 DEFAULT_PORT(8888)를 사용합니다.
        this(DEFAULT_PORT);
    }

    public MessageServer(int port) {
        // port <=0 이면 IllegalArgumentException이 발생합니다.
        if (port <= 0) {
            throw new IllegalArgumentException();
        }

        // port를 초기화 합니다.
        this.port = port;

        // port를 이용해서 serverSocket을 생성합니다.
        try {
            serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {

            // client가 serverSocket에 연결될 때 까지 대기합니다.
            try (Socket client = this.serverSocket.accept();
                 // client로 부터 전달 되는 stream 데이터를 처리하기 위해서 BufferedReader를 초기화 합니다.
                 BufferedReader clientIn = new BufferedReader(new InputStreamReader(client.getInputStream()));

                 /*
                  * server가 client에게 응답하기 위해서 PrintWriter를 이용해서 메시지를 전송합니다.
                  * PrintWriter를 초기화 합니다.
                  * printWriter 객체를 생성할 때 autoFlush에 대한 설정이 있습니다.(아래 링크를 참조 합니다.)
                  * https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/io/PrintWriter.html
                  */
                  PrintWriter clientOut = new PrintWriter(client.getOutputStream())) {
                
                /*
                 * cleint의 address(IP), PORT 를 로그로 출력합니다.
                 * - client socket을 이용해서 inetAddress를 구합니다.
                 * - InetAddress를 이용해서 address를 구합니다.
                 * - client socket을 이용해서 port를 구합니다.
                 */
                InetAddress inetAddress = client.getInetAddress();
                String address = inetAddress.getHostAddress();
                int port = client.getPort();
                log.debug("ip:{},port:{}", address, port);

                // recvMessage는 clent가 server로 전송하는 message를 받기 위한 변수를 선언합니다.
                String recvMessage = null;

                /*
                 * 즉 cleint 전송 받은 message가 null이 아니면
                 * client로부터 전송 받은 recvMessage를 다시 client에게 전송합니다.
                 * - printWriter의 println() method를 이용해서 client에게 message를 전송합니다.
                 */
                while ((recvMessage = clientIn.readLine()) != null) {
                    System.out.println("[server]recv-message:" + recvMessage);

                    clientOut.println(recvMessage);
                    clientOut.flush();
                }
            } catch (Exception e) {
                log.debug("{}", e.getMessage());
                if (e instanceof InterruptedIOException) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }// end method
}