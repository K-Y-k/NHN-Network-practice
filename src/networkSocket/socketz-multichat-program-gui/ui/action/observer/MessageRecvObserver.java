import java.util.Objects;

public class MessageRecvObserver implements Observer {
    private final MessageAction messageAction;

    public MessageRecvObserver(MessageAction messageAction) {
        if (messageAction == null){
            throw new IllegalArgumentException("messageAction is null");
        }
 
        // MessageRecvObserver 초기화 합니다.
        this.messageAction = messageAction;
    }

    @Override
    public EventType getEventType() {
        // EventType.RECV 반환합니다.
        return EventType.RECV;
    }

    @Override
    public void updateMessage(String message) {
        // 실질적인 recv event에 대한 처리를 담당하는 messageAction.execute() method를 호출합니다.
        messageAction.execute(message);
    }

}
