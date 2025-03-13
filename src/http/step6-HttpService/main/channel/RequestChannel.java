import java.util.LinkedList;
import java.util.Queue;

public class RequestChannel {
    private final Queue<Executable> requestQueue;
    private static final long QUEUE_MAX_SIZE = 10;

    private final long queueSize ;

    public RequestChannel() {
        this(QUEUE_MAX_SIZE);
    }

    public RequestChannel(long queueSize){
        if (queueSize < 0) {
            throw new IllegalArgumentException("queueSize > 0");
        }

        this.queueSize = queueSize;
        this.requestQueue = new LinkedList<>();
    }

    public synchronized void addHttpJob(Executable executable){
        while (requestQueue.size() >= QUEUE_MAX_SIZE) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        requestQueue.add(executable);
        notifyAll();
    }

    public synchronized Executable getHttpJob(){
        while(requestQueue.isEmpty()){
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // notify()는 synchronized에 선언된 블록 내부에 어디 위치에 있든
        // 결국 synchronized 블록이 끝날 때 실행된다.
        notifyAll();
        return requestQueue.poll();
    }
}