import lombok.extern.slf4j.Slf4j;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
public class SimpleHttpServer {

    private final int port;
    private static final int DEFAULT_PORT = 8080;
    private final ServerSocket serverSocket;

    public SimpleHttpServer(){
        this(DEFAULT_PORT);
    }

    public SimpleHttpServer(int port) {
        // port < 0 IllegalArgumentException이 발생합니다. 적절한 Error Message를 작성하세요
        if (port < 0) {
            throw new IllegalArgumentException(String.format("invalid port :%d", port));
        }

        // serverSocket을 생성합니다.
        this.port = port;

        try {
            serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    public synchronized void start() throws IOException {
        try {
            // interrupt가 발생하면 application이 종료합니다.
            while (!Thread.currentThread().isInterrupted()) {
                // client가 연결될 때 까지 대기 합니다.
                Socket client = this.serverSocket.accept();

                // Client와 서버가 연결 되면 HttpRequestHandler를 이용해서 Thread을 생성하고 실행합니다.
                Thread thread = new Thread(new HttpRequestHandler(client));
                thread.start();
            }
        } catch (Exception e){
            log.debug("{},",e.getMessage());
        }
    }
}
