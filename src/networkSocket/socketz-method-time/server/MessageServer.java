import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

@Slf4j
public class MessageServer implements Runnable {
    private static final int DEFAULT_PORT = 8888;
    private final int port;
    private final ServerSocket serverSocket;

    public MessageServer() {
        this(DEFAULT_PORT);
    }

    public MessageServer(int port) {
        if (port <= 0) {
            throw new IllegalArgumentException(String.format("port:%d", port));
        }

        this.port = port;

        try {
            serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try (Socket client = serverSocket.accept();
                    BufferedReader clientIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    PrintWriter out = new PrintWriter(client.getOutputStream(), false);) {
                InetAddress inetAddress = client.getInetAddress();
                log.debug("ip:{},port:{}", inetAddress.getAddress(), client.getPort());

                String recvMessage = null;

                while ((recvMessage = clientIn.readLine()) != null) {
                    System.out.println("[server]recv-message:" + recvMessage);

                    // MethodParser를 이용해서 recvMessage를 파싱합니다.
                    MethodParser.MethodAndValue methodAndValue = MethodParser.parse(recvMessage);
                    log.debug("method:{},value:{}", methodAndValue.getMethod(), methodAndValue.getValue());

                    // ResponseFactory를 이용해서 methodAndValue.getMethod()에 해당되는 response를 얻습니다.
                    Response response = new Response() {

                        @Override
                        public String getMethod() {
                            return methodAndValue.getMethod();
                        }

                        @Override
                        public String execute(String value) {
                            throw new UnsupportedOperationException("Unimplemented method 'execute'");
                        }
                        
                    };

                    String sendMessage;
                    if (Objects.nonNull(response)) {
                        // methodAndValue.getValue() 이용해서 response를 실행합니다.
                        sendMessage = methodAndValue.getValue();
                    } else {
                        // response가 null 이면 sendMessage를 "{echo} method not found" 로 설정합니다.
                        if (response == null) {
                            sendMessage = "{echo} method not found";
                        }

                        sendMessage = "something";
                    }
                    out.println(sendMessage);
                    out.flush();
                }
            } catch (Exception e) {
                log.debug("{}", e.getMessage(), e);
            }
        }
    }
}