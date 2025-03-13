import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
public class NotFoundHttpService implements HttpService{
    /* NotFoundHttpService 구현
        - 페이지를 찾을 수 없을 때 /resources/404.html 응답합니다.
        - httpStatusCode : 404
        - Description: Not Found
     */
    @Override
    public void doGet(HttpRequest httpRequest, HttpResponse httpResponse) {
        //Body-설정
        String responseBody = null;
        try {
            responseBody = ResponseUtils.tryGetBodyFromFile(ResponseUtils.DEFAULT_404);
        } catch (IOException e) {
            throw new RuntimeException();
        }


        //Header-설정
        String responseHeader = ResponseUtils.createResponseHeader(ResponseUtils.HttpStatus.NOT_FOUND.getCode(), "utf-8", responseBody.getBytes().length);

        
        //PrintWriter 응답
        try(PrintWriter bufferedWriter = httpResponse.getWriter();){
            bufferedWriter.write(responseHeader);
            bufferedWriter.write(responseBody);
            bufferedWriter.flush();
            log.debug("body: {}", responseBody.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
