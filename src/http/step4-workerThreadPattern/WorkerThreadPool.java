import java.util.Objects;

public class WorkerThreadPool {
    private final int poolSize;

    private final static int DEFAULT_POOL_SIZE=5;

    private final Thread[] workerThreads;
    private final RequestChannel requestChannel;

    public WorkerThreadPool(RequestChannel requestChannel){
        this(DEFAULT_POOL_SIZE, requestChannel);
    }
    public WorkerThreadPool(int poolSize, RequestChannel requestChannel) {
        // poolSize < 1 이면 IllegalArgumentException이 발생합니다. 적절히 ErrorMessage를 작성하세요
        if (poolSize < 1) {
            throw new IllegalArgumentException("Invalid: poolSize < 1");
        }

        // requestChannel null check. 적절히 ErrorMessage를 작성하세요
        if (Objects.isNull(requestChannel)) {
            throw new IllegalArgumentException("Invalid: requestChannel is null");
        }

        // poolSize, requestChannel 초기화
        this.poolSize = poolSize;
        this.requestChannel = requestChannel;

        // requestChannel을 이용하여 httpRequestHandler 객체를 생성합니다.
        HttpRequestHandler httpRequestHandler = new HttpRequestHandler(this.requestChannel);

        // workerThreads를 초기화 합니다. poolSize 만큼 Thread를 생성합니다.
        workerThreads = new Thread[this.poolSize];

        for (int i = 0; i < poolSize; i++) {
            // workerThread 생성및 이름 설정 :  thread-1,thread-2, thread-3 ...
            workerThreads[i] = new Thread(httpRequestHandler);
            workerThreads[i].setName(String.format("thread-%d", i + 1));

        }
    }
    public synchronized void start(){
        // workerThreads에 초가화된 모든 Thread를 start 합니다.
        for (Thread thread : workerThreads) {
            thread.start();
        }
    }

    public synchronized void stop(){
        /* interrupt()를 실행해서 thread를 종료합니다.
            - thread가 종료되는 과정에서 동기화 되어야 합니다.
         */
        for (int i = 0; i < poolSize; i++) {
            workerThreads[i].interrupt();
        }


        // join()를 호출해서 모든 thread가 종료될 떄까지 대기합니다.
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            throw new RuntimeException();
        }
    }
}
