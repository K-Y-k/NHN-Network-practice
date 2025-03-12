import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class HttpRequestHandlerTest {

    @Test
    @DisplayName("RequestChannel = null")
    void constructorTest(){
        // RequestHandler 객체생성시 channel이 null 이면 IllegalArgumentException 발생하는지 검증합니다.
        Assertions.assertThrows(IllegalArgumentException.class, ()-> {
            new HttpRequestHandler(null);
        });
    }

    @Test
    @DisplayName("producer & consumer")
    void run(){
        // requestChannel과 requestHandler 객체를 생성합니다.
        RequestChannel requestChannel = new RequestChannel();
        HttpRequestHandler requestHandler = new HttpRequestHandler(requestChannel);

        AtomicInteger counter = new AtomicInteger();

        // couter.incrementAndGet(); 호출하는 countExecutable 구현합니다.
        Executable countExecutable = new Executable() {
            @Override
            public void execute() {
                counter.incrementAndGet();
            }
        };

        // 생산자 requestChannel 실행할 작업(countExecutable)을 1초에 한 번씩 총 5회 추가합니다.
        Thread producer = new Thread(()->{
            log.debug("실행");
            for (int i = 0; i < 5; i++) {
                try {
                    Thread.sleep(100);
                    requestChannel.addHttpJob(countExecutable);
                    log.debug("횟수:{}", i);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                
            }
        });
        producer.start();

        // requestHandler를 이용해서 consumer thread를 생성하고 실행합니다.
        Thread consumer= new Thread(requestHandler);
        consumer.start();


        // producer(생산자)의 작업이 끝나지 않았다면 테스트를 실행하는 main Thread는 양보(대기) 합니다.
        if (producer.isAlive()) {
            Thread.yield();
        }

        log.debug("counter:{}", counter.get());

        Assertions.assertEquals(5,counter.get());
    }
}