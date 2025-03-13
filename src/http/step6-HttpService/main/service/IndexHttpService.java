import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

@Slf4j
public class IndexHttpService implements HttpService{
    /* 
     * /index.html을 처리하는 HttpService 구현체 입니다.
     *  - doGet()method를 구현 합니다.
     */
    @Override
    public void doGet(HttpRequest httpRequest, HttpResponse httpResponse) {=
        // Body-설정
        String responseBody = null;
        try {
            // filepath인 httpRequest.getRequestURI()를
            // ResponseUtils.class.getResourceAsStream()에 넣어 리소스 파일을 읽고 문자로 읽어서 반환합니다.
            responseBody = ResponseUtils.tryGetBodyFromFile(httpRequest.getRequestURI());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        

        //Header-설정
        String responseHeader = ResponseUtils.createResponseHeader(200, "utf-8", responseBody.getBytes().length);


        //PrintWriter 응답
        try(PrintWriter bufferedWriter = httpResponse.getWriter()){
            bufferedWriter.write(responseHeader);
            bufferedWriter.write(responseBody);
            bufferedWriter.flush();;

            log.info("body: {}", responseBody.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
