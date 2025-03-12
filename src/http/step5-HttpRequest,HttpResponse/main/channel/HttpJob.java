import com.nhnacademy.http.request.HttpRequest;
import com.nhnacademy.http.request.HttpRequestImpl;
import com.nhnacademy.http.response.HttpResponse;
import com.nhnacademy.http.response.HttpResponseImpl;
import com.nhnacademy.http.util.ResponseUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.util.Objects;

@Slf4j
public class HttpJob implements Executable {

    private final HttpRequest httpRequest;
    private final HttpResponse httpResponse;

    private final Socket client;

    public HttpJob(Socket client) {
        /* client null check, IllegalArgumentException 발생합니다. 적절한 ErrorMessage를 작성하세요
            httpRequest, httpResponse, client 초기화 합니다.
         */
        if (client == null) {
            throw new IllegalArgumentException("Invalid: client == null");
        }

        this.httpRequest = new HttpRequestImpl(client);
        this.httpResponse = new HttpResponseImpl(client);
        this.client = client;
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    @Override
    public void execute(){

        log.debug("method:{}", httpRequest.getMethod());
        log.debug("uri:{}", httpRequest.getRequestURI());
        log.debug("clinet-closed:{}",client.isClosed());

        String responseBody = null;
        String responseHeader = null;

        /* /index.html을 요청시  httpRequest.getRequestURI()에 해당되는 html 파일이 존재 하지 않는다면  Http Status Code : 404 Not Found 응답 합니다.
             - ex) /index.html 요청이 온다면 ->  /resources/index.html이 존재하지 않는다면 404 응답합니다.
             - ResponseUtils.isExist(httpRequest.getRequestURI()) 이용하여 구현합니다.
             - ResponseUtils.tryGetBodyFromFile() - responseBody에 응답할 html 파일을 읽습니다
             - ResponseUtils.createResponseHeader() - responseHeader 를 생성 합니다.
        */
        if (!ResponseUtils.isExist(httpRequest.getRequestURI())){
            // 404 - not -found
            try {
                responseBody = ResponseUtils.tryGetBodyFromFile(httpRequest.getRequestURI());
                responseHeader = ResponseUtils.createResponseHeader(404,"utf-8", responseBody.getBytes("utf-8").length);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else{
            //파일이 존재한다면
            /* responseBody에 응답할 html 파일을 읽습니다.
               - ResponseUtils.tryGetBodyFromFile(httpRequest.getRequestURI()) 이용하여 구현합니다.
            */
            try {
                responseBody = ResponseUtils.tryGetBodyFromFile(httpRequest.getRequestURI());
            } catch (IOException e) {
                throw new RuntimeException();
            }

            try {
                responseHeader = ResponseUtils.createResponseHeader(404,"utf-8", responseBody.getBytes("utf-8").length);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException();
            }
        }

        // BufferWriter를 사용 하여 responseHeader, responseBody를 client에게 응답합니다.
        try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));) {
            bufferedWriter.write(responseHeader);
            bufferedWriter.write(responseBody);
            bufferedWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            // client에게 응답 후 cleint와 연결을 종료합니다.
            try {
                if (Objects.nonNull(client) && client.isConnected()) {
                    client.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }        
    }
}