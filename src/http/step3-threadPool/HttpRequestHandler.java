import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.security.cert.CRL;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

import javax.sound.sampled.Line;

@Slf4j
public class HttpRequestHandler implements Runnable {

    private final Queue<Socket> requestQueue;
    private final int MAX_QUEUE_SIZE=10;

    private static final String CRLF="\r\n";

    public HttpRequestHandler() {
        // requestQueue를 초기화 합니다. Java에서 Queue의 구현체인 LinkedList를 사용합니다.
        requestQueue = new LinkedList<>();
    }

    public synchronized void addRequest(Socket client){
        /* queueSize >= MAX_QUEUE_SIZE 대기 합니다.
            즉 queue에 데이터가 소비될 때 까지 client Socket을 Queue에 등록하는 작업을 대기합니다.
        */
        while (requestQueue.size() >= MAX_QUEUE_SIZE) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // requestQueue에 client를 추가합니다.
        requestQueue.add(client);

        // 대기하고 있는 Thread를 깨웁니다.
        notifyAll();

    }

    public synchronized Socket getRequest(){
        // requestQueue가 비어 있다면 대기 합니다.
        while (requestQueue.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // 대기하고 있는 Thread를 깨우고, requestQueue에서 client를 반환합니다.
        notifyAll();
        return requestQueue.poll();
    }

    @Override
    public void run() {

        while (true) {
            // getRequest()를 호출하여 client를 requestQueue로 부터 얻습니다., requestQueue가 비어있다면 대기 합니다.
            Socket client = getRequest();
            
            // 다음과 같은 message가 응답되도록 구현 합니다.
            //<html><body><h1>{threadA}:hello java</h1></body></html>
            //<html><body><h1>{threadB}:hello java</h1></body></html>

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
                responseBody.append(String.format("<h1>{%s}:hello java</h1>", Thread.currentThread().getName()));
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
                log.error("socket error : {}", e);
            } finally {
                if (Objects.nonNull(client)){
                    try {
                        client.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

        }
    }
}
