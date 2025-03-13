import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
public class MethodNotAllowedService implements HttpService{

    /* MethodNotAllowdService 구현
        - index.html->doGet() 구현 되어 있습니다. -> POST 요청을 하면 405 method not allowd 응답합니다.
        - httpStatusCode : 405
        - Description: Method Not Allowed
        - /resources/405.html 응답합니다.
     */
    @Override
    public void service(HttpRequest httpRequest, HttpResponse httpResponse) {
        String method = httpRequest.getMethod();

        // Body-설정
        String responseBody = null;
        try {
            responseBody = ResponseUtils.tryGetBodyFromFile(ResponseUtils.DEFAULT_405);
        } catch (IOException e) {
            throw new RuntimeException();
        }

        // Header-설정
        String responseHeader = ResponseUtils.createResponseHeader(ResponseUtils.HttpStatus.METHOD_NOT_ALLOWED.getCode(), "utf-8", responseBody.getBytes().length);

        //PrintWriter 응답
        try (PrintWriter bufferedWriter = httpResponse.getWriter();){
            bufferedWriter.write(responseHeader);
            bufferedWriter.write(responseBody);
            bufferedWriter.write("\n");
            bufferedWriter.flush();
            log.debug("body: {}", responseBody.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
