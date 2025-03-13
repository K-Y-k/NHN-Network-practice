import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class InfoHttpServiceTest {

    HttpService httpService;
    HttpRequest httpRequest;
    HttpResponse httpResponse;
    PrintWriter printWriter;
    StringWriter stringWriter;

    @BeforeEach
    void setUp() throws IOException {
        httpService = new IndexHttpService();

        httpRequest = Mockito.mock(HttpRequestImpl.class);
        Mockito.when(httpRequest.getRequestURI()).thenReturn("/info.html");

        httpResponse = Mockito.mock(HttpResponseImpl.class);

        // StringWriter를 사용하여 커스텀 버퍼 생성
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        Mockito.when(httpResponse.getWriter()).thenReturn(printWriter);
    }

    @Test
    @DisplayName("instance of HttpService")
    void constructor(){
        Assertions.assertInstanceOf(HttpService.class, new InfoHttpService());
    }

    @Test
    @DisplayName("doGet")
    void doGet() {
        Mockito.when(httpRequest.getMethod()).thenReturn("GET");

        httpService.service(httpRequest,httpResponse);
        String response = stringWriter.toString();

        log.debug("response:{}",response);

        // response 검증, httpStatuscode: 200, description: OK 검증합니다.
        Assertions.assertAll(
            ()->{
                Assertions.assertTrue(response.contains(String.valueOf(ResponseUtils.HttpStatus.OK.getCode())));
            },
            ()->{
                Assertions.assertTrue(response.contains(String.valueOf(ResponseUtils.HttpStatus.OK.getDesription())));
            }
        );
    }

    @Test
    @DisplayName("doPost : 405 method not allowed")
    void doPost(){
        Mockito.when(httpRequest.getMethod()).thenReturn("POST");
        
        // response 검증,  request method = POST, RuntimeException이 발생합니다.
        Assertions.assertThrows(RuntimeException.class,()->{
            httpService.service(httpRequest,httpResponse);
        });
    }
}