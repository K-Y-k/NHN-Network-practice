import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

import ch.qos.logback.core.util.StringUtil;

@Slf4j
public class SimpleHttpServer {

    private final int port;
    private static final int DEFAULT_PORT = 8080;
    private final ServerSocket serverSocket;

    // CRLF를 선언합니다. 
    // CRLF는 Carriage Return (CR) 와 Line Feed (LF)를 의미하며, 
    // HTTP 프로토콜에서 줄 바꿈을 나타내기 위해 사용됩니다. 
    // 이는 \r\n으로 표현됩니다.
    private static final String CRLF = "\r\n";

    public SimpleHttpServer(){
        // 기본 port는 DEFAULT_PORT을 사용합니다.
        this(DEFAULT_PORT);
    }

    public SimpleHttpServer(int port) {
        // port range <=0 IllegalArgumentException 예외가 발생 합니다. 적절한 Error Message를 작성해주세요.
        if (port <= 0) {
            throw new IllegalArgumentException(String.format("port range check: %d", port));
        }

        this.port = port;

        // serverSocket을 초기화 합니다.
        try {
            this.serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    public void start() throws IOException {
        while (true) {

            try ( 
                // client가 연결될 때 까지 대기합니다.
                Socket client = this.serverSocket.accept();

                // 입출력을 위해서 Reader, Writer를 선언합니다.
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                ) {

                StringBuilder requestBuilder = new StringBuilder();
                log.debug("------HTTP-REQUEST_start()");

                while (true) {
                    String line = bufferedReader.readLine();
                    // requestBuilder에 append 합니다.
                    requestBuilder.append(line);
                    log.debug("{}", line);

                    // 종료 조건 null or size==0
                    if (line == null || line.length() == 0) {
                        break;
                    }
                }

                log.debug("------HTTP-REQUEST_end()");

                // clinet에 응답할 html을 작성합니다.
                /*
                    <html>
                        <body>
                            <h1>hello hava</h1>
                        </body>
                    </html>
                */

                StringBuilder responseBody = new StringBuilder();
                //html tag를 추가하세요.
                responseBody.append("<html>");
                responseBody.append("    <body>");
                responseBody.append("        <h1>hello java</h1>");
                responseBody.append("    </body>");
                responseBody.append("</html>");


                StringBuilder responseHeader = new StringBuilder();
                // HTTP/1.0 200 OK
                responseHeader.append(String.format("HTTP/1.0 200 OK%s",CRLF));
                responseHeader.append(String.format("Server: HTTP server/0.1%s", CRLF));
                // Content-type: text/html; charset="UTF-8"
                responseHeader.append(String.format("Content-type: text/html; charset=%s%s", "UTF-8", CRLF));
                // Connection: close 헤더를 사용하면 해당 요청 후에 연결이 닫히게 됩니니다.
                responseHeader.append(String.format("Connection: Closed%s", CRLF));
                // responseBody의  Content-Length를 설정합니다.
                responseHeader.append(String.format("Content-Length:%d %s%s",responseBody.toString().getBytes().length,CRLF,CRLF));

                // write Response Header
                bufferedWriter.write(responseHeader.toString());
                // write Response Body
                bufferedWriter.write(responseBody.toString());
                // buffer에 등록된 Response header와 body를 flush 합니다.
                // (socket을 통해서 clent에 응답 합니다.)
                bufferedWriter.flush();

                log.debug("header:{}", responseHeader);
                log.debug("body:{}", responseBody);

            } catch (IOException e) {
                log.error("socket error : {}", e);
            }
        }//end while
    }//end start
}
