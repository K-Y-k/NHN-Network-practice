import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
public class MethodNotAllowedIntegrationTest {
    private static final int TEST_PORT = 9999;
    static SimpleHttpServer simpleHttpServer;
    static Thread thread;


    @BeforeAll
    static void setUp() throws IOException {
        thread = new Thread(()->{
            simpleHttpServer = new SimpleHttpServer(TEST_PORT);
            simpleHttpServer.start();
        });

        thread.start();
    }


    @Test
    @DisplayName("doPost : 405 method not allowed , /index.html")
    void doPost1() throws URISyntaxException, IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        String url = String.format("http://localhost:%d/index.html",TEST_PORT);
        log.debug("url:{}",url);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());
        log.debug("response:{}",response.body());

        // response.statusCode() == 405 검증합니다.
        Assertions.assertEquals(405, response.statusCode());
    }

    @Test
    @DisplayName("doPost : 405 method not allowed , /info.html")
    void doPost2() throws URISyntaxException, IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        String url = String.format("http://localhost:%d/info.html",TEST_PORT);
        log.debug("url:{}",url);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());
        log.debug("response:{}",response.body());
        
        /// info.html은 doGet이구현 되어 있습니다. POST 요청을 했을 때 response.statusCode() == 405인지 검증합니다.
        Assertions.assertEquals(405, response.statusCode());
    }

    @AfterAll
    static void tearDown() throws InterruptedException {
        Thread.sleep(2000);
    }
}
