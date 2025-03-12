import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SimpleHttpServerTest {

    static Thread thread;
    static final int TEST_PORT = 9999;

    @BeforeAll
    static void beforeAllSetUp(){
        thread = new Thread(()->{
            SimpleHttpServer simpleHttpServer = new SimpleHttpServer(TEST_PORT);
            simpleHttpServer.start();
        });
        thread.start();
    }

    @Test
    @Order(1)
    @DisplayName("threadB - 홀수요청")
    void requestEvenNumber() throws URISyntaxException, IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(String.format("http://localhost:%d",TEST_PORT)))
                .build();

        HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());
        log.debug("response:{}",response.body());

        // threadB 문자열이 포함되었는지 검증합니다.
        Assertions.assertTrue(response.body().contains("threadB"));
    }

    @Test
    @Order(2)
    @DisplayName("threadB - 짝수요청")
    void requestOddNumber() throws URISyntaxException, IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(String.format("http://localhost:%d",TEST_PORT)))
                .build();

        HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());
        log.debug("response:{}",response.body());

        // threadA 문자열이 포함되었는지 검증합니다.
        Assertions.assertTrue(response.body().contains("threadA"));
    }

    @Test
    @Order(3)
    @DisplayName("status code : 200 ok")
    void request1() throws URISyntaxException, IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(String.format("http://localhost:%d",TEST_PORT)))
                .build();

        HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());
        log.debug("response:{}",response.body());

        // response.statusCode()인지 검증합니다.
        Assertions.assertEquals(response.statusCode(), 200);
    }

    @Test
    @Order(4)
    @DisplayName("response: hello java")
    void request2() throws URISyntaxException, IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request  = HttpRequest.newBuilder()
                .uri(new URI(String.format("http://localhost:%d", TEST_PORT)))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        log.debug("response:{}",response.body());

        // response.body()에 'hello' or 'java' 문자열이 포함되는지 검증합니다.
        Assertions.assertAll(
            () -> {
                Assertions.assertTrue(response.body().contains("hello"));
            },
            () -> {
                Assertions.assertTrue(response.body().contains("java"));
            }
        );
    }

    @Test
    @Order(5)
    @DisplayName("content-type")
    void request3() throws URISyntaxException, IOException, InterruptedException {
        // Content-Type header가 text/html 인지 검증합니다.
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(String.format("http://localhost:%d", TEST_PORT)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Optional<String> header = response.headers().firstValue("Content-Type");
        String actual = header.get().toLowerCase();

        Assertions.assertTrue(actual.contains("text/html"));
    }

    @Test
    @Order(6)
    @DisplayName("charset utf-8")
    void request4() throws URISyntaxException, IOException, InterruptedException {
        // charset이 utf-8인지 검증합니다.
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(String.format("http://localhost:%d", TEST_PORT)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Optional<String> header = response.headers().firstValue("Content-Type");
        String actual = header.get().toLowerCase();

        Assertions.assertTrue(actual.contains("charset=utf-8"));
    }

    @AfterAll
    static void tearDown() throws InterruptedException {
       Thread.sleep(1000);
    }

}