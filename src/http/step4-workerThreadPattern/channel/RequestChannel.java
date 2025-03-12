import java.util.LinkedList;
import java.util.Queue;

public class RequestChannel {
    private final Queue<Executable> requestQueue;
    private static final long QUEUE_MAX_SIZE = 10;

    private final long queueSize;

    public RequestChannel() {
        this(QUEUE_MAX_SIZE);
    }

    public RequestChannel(long queueSize){
        // queueSize < 0  IllegalArgumentException 발생합니다. 적절히 Error Message를 작성하세요.
        if(queueSize < 0) {
            throw new IllegalArgumentException("Queue Size is Wrong!!");
        }

        // queueSize, requestQueue를 초기화 합니다.
        this.queueSize = queueSize;
        this.requestQueue = new LinkedList<>();
    }

    public synchronized void addHttpJob(Executable executable){
         /* queueSize >= MAX_QUEUE_SIZE 대기합니다.
            즉 queue에 데이터가 소비될 때 까지 client Socket을 Queue에 등록하는 작업을 대기합니다.
          */
        while (requestQueue.size() >= QUEUE_MAX_SIZE) {
            try {
                wait();
            }
            catch(InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // requestQueue에 executable를 추가 합니다.
        requestQueue.add(executable);

        // 대기하고 있는 Thread를 깨웁니다.
        notifyAll();
    }

    public synchronized Executable getHttpJob(){
        // requestQueue가 비어 있다면 대기합니다.
        while (requestQueue.size() <= 0) {
            try {
                wait();
            }
            catch(InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // 대기하고 있는 Thread를 깨우고, requestQueue에서 Executable을 반환합니다.
        Executable executable = requestQueue.poll();
        notifyAll();

        return executable;
    }
}
