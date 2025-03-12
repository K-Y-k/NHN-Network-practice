import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class HttpRequestImpl implements HttpRequest {
    /* HttpRequest를 구현합니다.
    *  test/java/com/nhnacademy/http/request/HttpRequestImplTest TestCode를 실행하고 검증합니다.
    */

    private final Socket client;
    
    private String METHOD;
    private String requestURI;

    private Map<String, String> queryStringMap = new ConcurrentHashMap<>();
    private Map<String, String> headerMap = new ConcurrentHashMap<>();
    private Map<String, Object> attributeMap = new ConcurrentHashMap<>();

    public HttpRequestImpl(Socket client) {
        this.client = client;

        initial();
    }

    private void initial() {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
            // 요청메시지 첫줄
            String requestLine = bufferedReader.readLine();
            log.debug("읽어온 request 첫줄 라인 : {}", requestLine);

            // 메소드 가공
            String[] methodSplit = requestLine.split(" /");
            METHOD = methodSplit[0];

            // URI 가공
            String[] uriSplit = requestLine.split(" ");
            requestURI = uriSplit[1].split("\\?")[0];

            // 쿼리 스트링 가공
            String[] queryStringLine1 = requestLine.split("\\?");
            String[] queryStringLine2 = queryStringLine1[1].split(" ");
            String[] queryStringArr = queryStringLine2[0].split("&");

            for (String queryStr : queryStringArr) {
                String[] query = queryStr.split("=");
                queryStringMap.put(query[0], query[1]);
            }


            // 요청메시지 두번째 줄 부터는 요청메시지 헤더 부분을 가공
            while ((requestLine = bufferedReader.readLine()) != null) {
                log.debug("읽어온 request 헤더 라인 : {}", requestLine);

                String[] headerSplit = requestLine.split(": ");
                headerMap.put(headerSplit[0], headerSplit[1]);
            }
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public String getMethod() {
        return METHOD;
    }

    @Override
    public String getParameter(String name) {
        return queryStringMap.get(name);
    }

    @Override
    public Map<String, String> getParameterMap() {
        return queryStringMap;
    }

    @Override
    public String getHeader(String name) {
        return headerMap.get(name);
    }

    @Override
    public void setAttribute(String name, Object o) {
        attributeMap.put(name, o);
    }

    @Override
    public Object getAttribute(String name) {
        if(attributeMap.containsKey(name)) {
            return attributeMap.get(name);
        }

        return null;
    }

    @Override
    public String getRequestURI() {
        return requestURI;
    }
}
