import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.Optional;

@Slf4j
class SimpleHttpServerTest {
    static SimpleHttpServer simpleHttpServer;

    static int TEST_PORT = 9999;

    @BeforeAll
    static void beforeAllSetUp(){
        Thread thread = new Thread(
                ()->{
                    simpleHttpServer = new SimpleHttpServer(TEST_PORT);
                    try {
                        simpleHttpServer.start();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
        thread.start();

    }

    @Test
    @DisplayName("status code : 200 ok")
    void request1() throws URISyntaxException, IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(String.format("http://localhost:%d",TEST_PORT)))
                .build();

        HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());
        log.debug("response:{}",response.body());

        // response.statusCode() == 200 검증합니다.
        Assertions.assertEquals(200, response.statusCode());
    }

    @Test
    @DisplayName("response: hello java")
    void request2() throws URISyntaxException, IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(String.format("http://localhost:%d",TEST_PORT)))
                .build();

        HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());

        // response.body() 'hello' or 'java' 문자열이 포함되었는지 검증합니다.
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
    @DisplayName("content-type")
    void request3() throws URISyntaxException, IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(String.format("http://localhost:%d",TEST_PORT)))
                .build();

        HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());
        Optional<String> contentTypeOptional = response.headers().firstValue("Content-Type");
        String actual = contentTypeOptional.get().toLowerCase();
        log.debug("contentType:{}",actual);

        //contentType이 'text/html' 검증합니다.
        Assertions.assertTrue(actual.contains("text/html"));
    }

    @Test
    @DisplayName("charset utf-8")
    void request4() throws URISyntaxException, IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(new URI(String.format("http://localhost:%d", TEST_PORT)))
            .build();
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        Optional<String> contentTypeOptional = response.headers().firstValue("Content-Type");
        String actual = contentTypeOptional.get().toLowerCase();

        // contentType header의 charset=utf-8 인지 검증합니다.
        Assertions.assertTrue(actual.contains("charset=utf-8"));
    }


    @Test
    @DisplayName("Content-Length")
    void request5() throws URISyntaxException, IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(new URI(String.format("http://localhost:%d", TEST_PORT)))
            .build();
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        Optional<String> contentTypeOptional = response.headers().firstValue("Content-Length");
        String actual = contentTypeOptional.get().toLowerCase();

        // content-Length 값이 존재하는지 검증합니다.
        Assertions.assertTrue(actual != null && !actual.isBlank());
    }

    @AfterAll
    static void tearDown() throws InterruptedException {
        Thread.sleep(1000);
    }

}