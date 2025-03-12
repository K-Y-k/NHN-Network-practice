import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicLong;


@Slf4j
public class SimpleHttpServer {

    private final int port;
    private static final int DEFAULT_PORT=8080;

    private final AtomicLong atomicCounter;

    public SimpleHttpServer(){
        this(DEFAULT_PORT);
    }
    public SimpleHttpServer(int port) {
        // port <=0 이면 IllegalArgumentException 발생합니다. 적절한 Error Message를 작성하세요.
        if (port <= 0) {
            throw new IllegalArgumentException("port <= 0");
        }

        // port와 atomicCounter를 초기화 합니다.
        this.port = port;
        atomicCounter = new AtomicLong(0);
    }

    public void start(){
        try (ServerSocket serverSocket = new ServerSocket(port);) {

            HttpRequestHandler httpRequestHandlerA = new HttpRequestHandler();
            HttpRequestHandler httpRequestHandlerB = new HttpRequestHandler();

            // threadA를 생성하고 시작 합니다. thread-name : threadA 설정합니다.
            Thread threadA = new Thread(httpRequestHandlerA);
            threadA.setName("threadA");
            threadA.start();

            // threadB를 생성하고 시작 합니다. thread-name: threadB 설정합니다.
            Thread threadB = new Thread(httpRequestHandlerB);
            threadB.setName("threadB");
            threadB.start();

            while (true){
                Socket client = serverSocket.accept();

                /*count값이 짝수이면 httpRequestHandlerA에 client를 추가합니다.
                  count값이 홀수라면 httpRequestHandlerB에 clinet를 추가합니다.
                */
                long count = atomicCounter.incrementAndGet();

                if (count % 2 == 0) {
                    httpRequestHandlerA.addRequest(client);
                } else {
                    httpRequestHandlerB.addRequest(client);
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
