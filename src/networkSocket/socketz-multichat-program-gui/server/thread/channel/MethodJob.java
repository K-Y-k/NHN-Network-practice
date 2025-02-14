import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Objects;

@Slf4j
public class MethodJob implements Executable {
    private final Socket client;

    public MethodJob(Socket client) {
        // client null 이거나 closed 상태이면 IllegalArgumentException 발생
        if (client == null || client.isClosed()) {
            throw new IllegalArgumentException();
        }

        this.client = client;
    }

    @Override
    public void execute() {
        // execute method 실행시 Session.initializeSocket()를 호출해서 client socket을 등록합니다.
        Session.initializeSocket(this.client);

        // BufferedReader, PrintWriter 초기화 합니다.
        try (
             BufferedReader clientIn = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
             PrintWriter out = new PrintWriter(this.client.getOutputStream(), false);
        ) {
            InetAddress inetAddress = client.getInetAddress();
            log.debug("ip:{},port:{}", inetAddress.getAddress(), client.getPort());

            String recvMessage;

            while ((recvMessage = clientIn.readLine()) != null) {
                System.out.println("recv-message: " + recvMessage);

                // client로 부터 전달된 message를 MethodParser를 이용해서 파싱합니다.
                MethodParser.MethodAndValue methodAndValue = MethodParser.parse(recvMessage);
                log.debug("method:{},value:{}", methodAndValue.getMethod(), methodAndValue.getValue());

                // ResponseFactory를 이용해서 실행할 Response를 생성합니다.
                Response response = ResponseFactory.getResponse(methodAndValue.getMethod());

                // response 실행하고 결과를 client에게 응답합니다.
                String sendMessage;
                if (Objects.nonNull(response)) {
                    sendMessage = response.execute(methodAndValue.getValue());
                } else {
                    sendMessage = String.format("{%s} method not found!", methodAndValue.getMethod());
                }

                out.println(sendMessage);
                out.flush();
            }
        } catch (Exception e) {
            log.debug("thread-error:{}",e.getMessage(),e);
        } finally {
            // client socket이 null 아니면 client.close()를 호출합니다.
            try {
                if(Objects.nonNull(client)){
                    client.close();
                    log.debug("client 정상종료");

                    /* 
                     * 로그인이 되어 있다면 client socket을 clientMap에서 제거합니다.
                     */
                    if (Session.isLogin()) {
                        MessageServer.removeClient(Session.getCurrentId());
                    }

                }
            } catch (IOException e) {
                log.error("error-client-close : {}",e.getMessage(),e);
            }

            // Session.reset()을 호출해서 초기화 합니다.
            Session.reset();
        }
    }
}
