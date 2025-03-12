import java.io.IOException;

// Executable interface를 구현한 객체는 WorkerThread(작업자)가 execute method를 호출합니다.
public interface Executable {
    void execute() throws IOException;
}