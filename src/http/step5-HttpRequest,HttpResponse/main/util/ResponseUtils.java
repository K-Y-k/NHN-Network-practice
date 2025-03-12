import java.io.*;
import java.net.URL;
import java.security.cert.CRL;
import java.util.Objects;

public class ResponseUtils {
    public static final String DEFAULT_404 = "/404.html";
    private static final String CRLF="\r\n";
    private ResponseUtils(){}

    enum HttpStatus{
        OK(200,"OK"),
        NOT_FOUND(404, "Not Found"),
        UNKNOWN(-1, "Unknown Status");

        private final int code;
        private final String desription;

        HttpStatus(int code, String description) {
            this.code = code;
            this.desription=description;
        }

        public int getCode() {
            return code;
        }

        public String getDesription() {
            return desription;
        }

        public static HttpStatus getStatusFromCode(int code) {
            for (HttpStatus status : HttpStatus.values()) {
                if (status.getCode() == code) {
                    return status;
                }
            }
            return UNKNOWN;
        }
    }

    /**
     * /src/main/resourcs 하위에 filePath에 해당되는 파일이 존재하는 체크 합니다.
     * @param filePath, filePath -> requestURl -> ex) /index.html
     * @return true or false
     */
    public static boolean isExist(String filePath){
        /* isExist를 구현합니다.
           ex) filePat=/index.html 이면 /resources/index.html이 존재하면 true, 존재하지 않다면 false를 반환합니다.
           ex) filePath=/ false를 반환합니다.
        */
        if (filePath.equals("/")){
            return false;
        }
        
        URL url = ResponseUtils.class.getResource(filePath);
        return Objects.nonNull(url);
    }

    /**
     * @param filePath , requestURi, ex) /index.html
     * @return String , index.html 파일을 읽고 String으로 반환
     * @throws IOException
     */
    public static String tryGetBodyFromFile(String filePath) throws IOException {
        /* tryGetBodyFromFile 구현합니다.
         * ex) filePath = /index.html -> /resources/index.html 파일을 읽어서 반환합니다.
         */

        StringBuilder responseBody = new StringBuilder();
        try(InputStream inputStream = ResponseUtils.class.getResourceAsStream(filePath);
            BufferedReader reader =  new BufferedReader(new InputStreamReader(inputStream,"UTF-8"))){
            while (true) {
                String line = reader.readLine();

                if(Objects.isNull(line)){
                    break;
                }

                responseBody.append(line);
            }
        }
        return responseBody.toString();
    }

    /**
     * @param httpStatusCode , 200 - OK
     * @param charset, default : UTF-8
     * @param contentLength, responseBody의 length
     * @return responseHeader를 String 반환
     */
    public static String createResponseHeader(int httpStatusCode, String charset, int contentLength ){
        /* responseHeader를 생성합니다. 아래 header 예시를 참고
            - 200 OK
            HTTP/1.0 200 OK
            Server: HTTP server/0.1
            Content-type: text/html; charset=UTF-8
            Connection: Closed
            Content-Length:143

            - 404 Not Found
            HTTP/1.0 404 Not Found
            Server: HTTP server/0.1
            Content-type: text/html; charset=UTF-8
            Connection: Closed
            Content-Length:143

            - HttpStatusCode는 HttpStatus enum을 참고하여 구현합니다.
        */

        StringBuilder responseHeader = new StringBuilder();
        responseHeader.append(String.format("HTTP/1.0 %d %s%s", httpStatusCode, HttpStatus.getStatusFromCode(httpStatusCode).getDesription(),CRLF));
        responseHeader.append(String.format("Server: HTTP server/0.1%s",CRLF));
        responseHeader.append(String.format("Content-type: text/html; charset=%s%s",charset,CRLF));
        responseHeader.append(String.format("Connection: Closed%s", CRLF));
        responseHeader.append(String.format("Content-Length:%d %s%s",contentLength,System.lineSeparator(),CRLF));
        return responseHeader.toString();
    }

}
