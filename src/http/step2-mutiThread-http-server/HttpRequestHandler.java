import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.util.Objects;

@Slf4j
/* 
 *Java에서 Thread는 implements Runnable or extends Thread를 이용해서 Thread를 만들 수 있습니다.
 *  implements Runnable을 사용하여 구현합니다.
 */
public class HttpRequestHandler implements Runnable {
    private final Socket client;

    private final static String CRLF="\r\n";

    public HttpRequestHandler(Socket client) {
        // 생성자를 초기화 합니다. cleint null or socket close 되었다면 적절히 Exception을 발생시킵니다.
        if (client == null || client.isClosed()) {
            throw new IllegalArgumentException();
        }

        this.client = client;
    }


    public void run() {
        // exercise-simple-http-server-step1을 참고 하여 구현합니다.
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
             BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()))) {
            
            StringBuilder requestBuilder = new StringBuilder();
            log.debug("------HTTP-REQUEST_start()-----");

            // 요청 메시지를 읽고 서버에 로그 출력
            while (true) {
                String line = bufferedReader.readLine();

                requestBuilder.append(line);
                log.debug("{} ", line);

                if (line == null || line.length() == 0) {
                    break;
                }
            }   

            log.debug("------HTTP-REQUEST_end()----");


            // 응답 메시지 바디 구성
            StringBuilder responseBody = new StringBuilder();
            responseBody.append("<html>");
            responseBody.append("    <body>");
            responseBody.append("        <h1>hello java</h1>");
            responseBody.append("    </body>");
            responseBody.append("</html>");

            // 응답 메시지 헤더 구성
            StringBuilder responseHeader = new StringBuilder();
            responseHeader.append(String.format("HTTP/1.0 200 OK%s", CRLF));
            responseHeader.append(String.format("Server: HTTP server/0.1%s", CRLF));
            responseHeader.append(String.format("Content-type: text/html; charset=%s%s", "UTF-8", CRLF));
            responseHeader.append(String.format("Connection: Closed%s", CRLF));
            responseHeader.append(String.format("Content-Length:%d %s%s", responseBody.toString().getBytes().length,CRLF,CRLF));


            // 버퍼에 등록 및 등록한 내용을 전송 및 비우기
            bufferedWriter.write(responseHeader.toString());
            bufferedWriter.write(responseBody.toString());
            bufferedWriter.flush();

            log.debug("header:{}", responseHeader);
            log.debug("body:{}", responseBody);
        } catch (IOException e) {
            log.error("socket error : {}", e);
        } finally {
            if (Objects.nonNull(client)){
                try {
                    //clinet 연결 종료
                    client.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
