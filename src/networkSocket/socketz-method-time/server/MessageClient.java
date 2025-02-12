import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Objects;

@Slf4j
public class MessageClient implements Runnable {
    private static final String DEFAULT_SERVER_ADDRESS = "localhost";
    private static final int DEFAULT_PORT = 8888;

    private final String serverAddress;
    private final int serverPort;

    private final Socket clientSocket;

    public MessageClient() {
        this(DEFAULT_SERVER_ADDRESS, DEFAULT_PORT);
    }

    public MessageClient(String serverAddress, int serverPort) {

        if (StringUtils.isEmpty(serverAddress) || serverPort <= 0) {
            throw new IllegalArgumentException();
        }

        this.serverAddress = serverAddress;
        this.serverPort = serverPort;

        try {
            clientSocket = new Socket(this.serverAddress, this.serverPort);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try (
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));) {

            System.out.print("send-message:");
            String userMessage;

            while ((userMessage = stdIn.readLine()) != null) {
                out.println(userMessage);
                System.out.println(String.format("[client]recv-message:%s", clientIn.readLine()));
                System.out.print("send-message:");
            }

        } catch (Exception e) {
            log.debug("message:{}", e.getMessage(), e);
            log.debug("client close");
        } finally {
            if (Objects.nonNull(clientSocket)) {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
