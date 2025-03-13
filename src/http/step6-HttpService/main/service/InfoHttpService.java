import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
public class InfoHttpService implements HttpService {
    /* InfoHttpService 구현
       - Request : http://localhost:8080/info.html?id=marco&age=40&name=마르코
       - 요청을 처리하고 응답하는 InfoHttpService 입니다.
       - IndexHttpService를 참고하여 doGet을 구현하세요.
       - info.html 파일은 /resources/info.html 위치합니다.
       - info.html을 읽어 parameters{id,name,age}를 replace 후 응답합니다.
       - ex)
            ${id} <- marco
            ${name} <- 마르코
            ${age} <- 40
    */

    @Override
    public void doGet(HttpRequest httpRequest, HttpResponse httpResponse) {
        // body-설정
        String responseBody = null;
        try {
            responseBody = ResponseUtils.tryGetBodyFromFile(httpRequest.getRequestURI());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        // 받아오는 값은 URL 인코딩된 형태
        // ex) 공백은 %20으로, 한글은 %EC%9D%80%EC%9D%98와 같은 형태로 인코딩
        String id =  httpRequest.getParameter("id");
        String name= httpRequest.getParameter("name");
        name = URLDecoder.decode(name, StandardCharsets.UTF_8);
        String age = httpRequest.getParameter("age");

        log.debug("id:{}",id);
        log.debug("name:{}",name);
        log.debug("age:{}",age);
    
        log.debug("responseBody: {}",responseBody);

        // 특정 텍스트(ex) ${id})를 다른 값으로 바꾸는 작업
        responseBody = responseBody.replace("${id}",id);
        responseBody = responseBody.replace("${name}",name);
        responseBody = responseBody.replace("${age}",age);


        // Header-설정
        String responseHeader = ResponseUtils.createResponseHeader(200, "utf-8", responseBody.getBytes().length);

        
        // PrintWriter를 이용한 응답
        try (PrintWriter bufferedWriter = httpResponse.getWriter();) {
            bufferedWriter.write(responseHeader);
            bufferedWriter.write(responseBody);
            bufferedWriter.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
