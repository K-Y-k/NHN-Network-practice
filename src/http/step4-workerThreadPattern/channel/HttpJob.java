import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.security.cert.CRL;
import java.util.Objects;

@Slf4j
public class HttpJob implements Executable {
    private final Socket client;
    private static final String CRLF="\r\n";

    public HttpJob(Socket client) {
        if (Objects.isNull(client)) {
            throw new IllegalArgumentException("client Socket is null");
        }

        this.client = client;
    }

    public Socket getClient() {
        return client;
    }

    @Override
    public void execute(){
        // HttpJob는 execute() method를 구현 합니다. step2~3 참고하여 구현합니다.
        //<html><body><h1>thread-0:hello java</h1></body>
        //<html><body><h1>thread-1:hello java</h1></body>
        //<html><body><h1>thread-2:hello java</h1></body>
        //....

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
             BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()))) {
            
            StringBuilder request = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                request.append(line);
            }

            StringBuilder responseBody = new StringBuilder();
            responseBody.append("<html>");
            responseBody.append("<body>");
            responseBody.append(String.format("<h1>%s:hello java</h1>", Thread.currentThread()));
            responseBody.append("</body>");
            responseBody.append("</html>");

            StringBuilder responseHeader = new StringBuilder();
            responseHeader.append(String.format("HTTP/1.0 200 OK%s", CRLF));
            responseHeader.append(String.format("Server: HTTP server/0.1%s", CRLF));
            responseHeader.append(String.format("Content-type: text/html; charset=%s%s", "UTF-8", CRLF));
            responseHeader.append(String.format("Connection: Closed%s", CRLF));
            responseHeader.append(String.format("Content-Length:%d %s%s", responseBody.length(), CRLF, CRLF));

            bufferedWriter.write(responseHeader.toString());
            bufferedWriter.write(responseBody.toString());

            bufferedWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
