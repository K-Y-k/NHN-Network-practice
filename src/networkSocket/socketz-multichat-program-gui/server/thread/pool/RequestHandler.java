import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RequestHandler implements Runnable {
    public RequestHandler(RequestChannel requestChannel) {
        // requestChannel을 초기화 합니다.
        this.requestChannel = requestChannel;
    }

    private final RequestChannel requestChannel;

    @Override
    public void run() {
        // thread interupted가 발생하면 종료 됩니다.
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // requestChannel로 부터 실행할 작업을 획득 후 execute method를 호출해서 실행합니다.
                Executable executable = requestChannel.getJob();
                executable.execute();

            } catch (Exception e){
                if(e instanceof InterruptedException){
                    log.debug("thread 종료!");
                    Thread.currentThread().interrupt();
                }
                log.error("thread-exception : {}", e.getMessage(),e);
            }
        }
    }
}
