import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessageSubject implements Subject {

    private final List<Observer> observers;

    public MessageSubject() {
        // Observer를 등록한 thread list를 생성 합니다. thread safety 해야 합니다.
        observers = Collections.synchronizedList(new ArrayList<>());
    }

    @Override
    public void register(EventType eventType, Observer observer) {
        // observers에 observer를 등록합니다.
        observers.add(observer);
    }

    @Override
    public void remove(EventType eventType, Observer observer) {
        // observers 에서 observer를 삭제합니다.
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(EventType eventType, String message) {
        /*
            모든 observer들에게 알림니다.
           - method type을 고려해야 합니다. SEND,RECV, observer.validate()를 사용하여 검증합니다.
           - observer.updateMessage(message)를 호출합니다.
        */
        for (Observer observer : observers){
            if (observer.validate(eventType)) {
                observer.updateMessage(message);
            }
        }
    }
}
