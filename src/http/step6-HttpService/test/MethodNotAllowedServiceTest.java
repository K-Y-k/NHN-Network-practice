import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

@Slf4j
class MethodNotAllowedServiceTest {
    HttpService httpService;
    HttpRequest httpRequest;
    HttpResponse httpResponse;
    PrintWriter printWriter;
    StringWriter stringWriter;

    @BeforeEach
    void setUp() throws IOException {
        httpService = new MethodNotAllowedService();

        httpRequest = Mockito.mock(HttpRequestImpl.class);
        Mockito.when(httpRequest.getRequestURI()).thenReturn("/index.html");

        httpResponse = Mockito.mock(HttpResponseImpl.class);

        // StringWriter를 사용하여 커스텀 버퍼 생성
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        Mockito.when(httpResponse.getWriter()).thenReturn(printWriter);
    }

    @Test
    @DisplayName("instance of HttpService")
    void constructor(){
        Assertions.assertInstanceOf(HttpService.class, new MethodNotAllowedService());
    }

    @Test
    @DisplayName("doGet : 405 method not allowed")
    void doGet() {
        Mockito.when(httpRequest.getMethod()).thenReturn("GET");

        httpService.service(httpRequest,httpResponse);

        String response = stringWriter.toString();
        log.debug("response:{}",response);

        // response 검증, httpStatuscode: 405, description: Method Not Allowed 검증합니다.
        Assertions.assertAll(
            ()->{
                Assertions.assertTrue(response.contains(String.valueOf(ResponseUtils.HttpStatus.METHOD_NOT_ALLOWED.getCode())));
            },
            ()->{
                Assertions.assertTrue(response.contains(String.valueOf(ResponseUtils.HttpStatus.METHOD_NOT_ALLOWED.getDesription())));
            }
        );
    }
}