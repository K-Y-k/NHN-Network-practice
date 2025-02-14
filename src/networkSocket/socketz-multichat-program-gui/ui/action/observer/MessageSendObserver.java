public class MessageSendObserver implements Observer {
    private final MessageAction messageAction;

    public MessageSendObserver(MessageAction messageAction) {
        // MessageSendObserver 초기화 합니다.
        this.messageAction = messageAction;
    }

    @Override
    public EventType getEventType() {
        // EventType.SEND 를 반환합니다.
        return EventType.SEND;
    }

    @Override
    public void updateMessage(String message) {
        // 실질적인 send event에 대한 처리를 담당하는 messageAction.execute() method를 호출합니다.
        messageAction.execute(message);
    }

}
