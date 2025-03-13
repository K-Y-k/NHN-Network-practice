import com.nhnacademy.http.request.HttpRequest;
import com.nhnacademy.http.request.HttpRequestImpl;
import com.nhnacademy.http.response.HttpResponse;
import com.nhnacademy.http.response.HttpResponseImpl;
import com.nhnacademy.http.service.*;
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
        
        HttpService httpService = null;

        /* RequestURI에 따른 HttpService를 생성합니다.
            - httpService.service(httpRequest, httpResponse) 호출하면
            - service()에서 Request Method에 의해서 doGet or doPost를 호출합니다
            - ex1) /test.html존재 하지 않는다면 NotFoundHttpService 를 httpService에 할당합니다.
            - ex2) /index.html -> IndexHttpService 객체를 httpService에 할당합니다.
            - ex3) /info.html -> InfoHttpService 객체를 httpService에 할당합니다.
        */
        String url = httpRequest.getRequestURI();

        /**
         * step6 이 방식의 문제점!
         * 1. 구현체를 new로 생성하는데 계속 생성되어 메모리 낭비 문제!! (개발자로서 제일 중요)
         * 2. uri가 새로 생길 때마다 분기문을 추가 해야하는 OCP 원칙 준수 문제
         */
        if (!ResponseUtils.isExist(url)) {
            httpService = new NotFoundHttpService();
        } else if (url.equals("/index.html")) {
            httpService = new IndexHttpService();
        } else if (url.equals("/index.html")) {
            httpService = new InfoHttpService();
        } else {
            httpService = new NotFoundHttpService();
        }


        // httpService.service() 호출합니다. 
        // 호출시 예외 Method Not Allowd 관련 Exception이 발생하면 httpService에 MethodNotAllowdService 객체를 생성해서 할당합니다.
        try {
            httpService.service(httpRequest, httpResponse);
        }catch (RuntimeException e){
            httpService = new MethodNotAllowedService();
            httpService.service(httpRequest, httpResponse);
        }

        // client 연결을 종료 합니다.
        try {
            if (Objects.nonNull(client) && client.isConnected()) {
                client.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
