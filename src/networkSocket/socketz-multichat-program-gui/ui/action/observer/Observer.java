public interface Observer {
    EventType getEventType();
    void updateMessage(String message);
    default boolean validate(EventType eventType){
        return getEventType().equals(eventType);
    }
}
