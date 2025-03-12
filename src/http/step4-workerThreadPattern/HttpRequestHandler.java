import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;

@Slf4j
public class HttpRequestHandler implements Runnable {

    private final RequestChannel requestChannel;

    public HttpRequestHandler(RequestChannel requestChannel) {
        if (Objects.isNull(requestChannel)) {
            throw new IllegalArgumentException("requestChannel is null");
        }
        this.requestChannel = requestChannel;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // requestChannel로 부터 httpJob을 할당 받습니다.
                Executable httpJob = requestChannel.getHttpJob();

                // httpJob 객체의 execute() method를 실행합니다.
                httpJob.execute();

            } catch (Exception e) {
                // 상위 레벨의 다른 코드 또는 스레드가 이 스레드가 인터럽트 되었음을 인지 할 수 있습니다.
                if(e.getMessage().contains(InterruptedException.class.getName())){
                    Thread.currentThread().interrupt();
                }
                // 종료될 떄 필요한 코드가 있다면 작성합니다.
                log.debug("RequestHandler error : {}",e.getMessage(),e);
            }
        }
    }
}
