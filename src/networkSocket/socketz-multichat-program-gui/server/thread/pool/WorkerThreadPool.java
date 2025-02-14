import java.util.Objects;

public class WorkerThreadPool {
    private final int poolSize;

    private final static int DEFAULT_POOL_SIZE=5;

    private final Thread[] workerThreads;
    private final Runnable runnable;

    public WorkerThreadPool(Runnable runnable){
        // 생성자를 초기화 합니다.
        this(DEFAULT_POOL_SIZE, runnable);
    }

    public WorkerThreadPool(int poolSize, Runnable runnable) {

        // poolSize <1 or runnable isNull 다면 IllegalArgumentException이 발생
        if (poolSize < 1 || runnable == null) {
            throw new IllegalArgumentException();
        }

        // poolSize, runnable 초기화 합니다.
        this.poolSize = poolSize;
        this.runnable = runnable;

        // workerThreads 초기화 합니다.
        workerThreads = new Thread[poolSize];

        // initilizePool() 호출합니다.
        initilizePool();
    }

    private void initilizePool(){
        // runnable를 이용해서 thread를 생성합니다.
        for (int i = 0; i < poolSize; i++) {
            workerThreads[i] = new Thread(this.runnable);
        }
    }

    public synchronized void start(){
        // workerThreads에 초가화된 모든 Thread를 start합니다.
        // start()는 동기화 처리되어야 합니다.
        for (Thread thread : workerThreads) {
            thread.start();
        }
    }

    public synchronized void stop(){
        // interrupt()를 발생시켜,  thread를 종료시킵니다.
        // stop() 동기화 처리되어야 합니다.
        for (Thread thread : workerThreads) {
            if(Objects.nonNull(thread) && thread.isAlive()){
                thread.interrupt();
            }
        }

        for (Thread thread : workerThreads){
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
