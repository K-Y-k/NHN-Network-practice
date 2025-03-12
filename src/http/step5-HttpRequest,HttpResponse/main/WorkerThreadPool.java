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
        // poolSize < 1 이면 IllegalArgumentException이 발생합니다.
        if (poolSize < 1) {
            throw new IllegalArgumentException("poolSize: > 0");
        }

        // requestChannel null check
        if (Objects.isNull(requestChannel)) {
            throw new IllegalArgumentException("requestChannel is null");
        }

        this.poolSize = poolSize;
        this.requestChannel = requestChannel;

        // requestChannel을 이용하여 httpRequestHandler 객체를 생성합니다.
        HttpRequestHandler httpRequestHandler = new HttpRequestHandler(requestChannel);

        // workerThreads를 초기화 합니다. poolSize 만큼 Thread를 생성합니다.
        workerThreads = new Thread[poolSize];

        for (int i = 0; i < poolSize; i++){
            // thread-1,thread-2 .... 이름설정
            workerThreads[i] = new Thread(httpRequestHandler);
            workerThreads[i].setName(String.format("thread-%d",i));
        }
    }

    public synchronized void start(){
        // workerThreads에 초가화된 모든 Thread를 start 합니다.
        for (Thread thread : workerThreads) {
            thread.start();
        }
    }

    public synchronized void stop(){
        /* interrupt()를 실행해서 thread를 종료 합니다.
            - thread가 종료되는 과정에서 동기화 되어야 합니다.
            - 우선 모든 thread interrupt 호출
         */
        for (Thread thread : workerThreads) {
            if (Objects.nonNull(thread) && thread.isAlive()) {
                thread.interrupt();
            }
        }

        // join()를 이용해서 모든 thread가 종료될 떄 까지 대기 상태로 만듭니다.
        for (Thread thread : workerThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
