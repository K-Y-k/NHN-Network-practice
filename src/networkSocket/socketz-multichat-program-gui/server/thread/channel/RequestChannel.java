import java.util.LinkedList;
import java.util.Queue;

public class RequestChannel {
    private final Queue<Executable> requestQueue;
    private long QUEUE_MAX_SIZE = 10;

    public RequestChannel() {
        // responseQueue를 LinkedList를 이용해서 초기화 합니다.
        this.requestQueue = new LinkedList<>();
    }

    public synchronized void addJob(Executable executable){
        // where 조건이 requestQueue.size() >= QUEUE_MAX_SIZE 이면 대기합니다.
        while (requestQueue.size() >= QUEUE_MAX_SIZE){
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException();
            }
        }

        // requestQueue에 executable을 추가히고 대기하고 있는 thread를 깨웁니다.
        requestQueue.add(executable);
        notify();
    }

    public synchronized Executable getJob(){
        // requestQueue.isEmpty() 이면 대기합니다.
        while (requestQueue.isEmpty()){
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException();
            }
        }

        // requestQueue에서 작업을 반환하고 대기하고 있는 thread를 깨웁니다.
        notify();
        return requestQueue.poll();
    }

}
