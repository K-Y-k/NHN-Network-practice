public class RecvMessageAction implements MessageAction {
    private final MessageClientForm messageClientForm;

    public RecvMessageAction(MessageClientForm messageClientForm) {
        // messageClientForm을 초기화 합니다. messageClientForm은 메시지 전송/수신 UI를 담당합니다.
        this.messageClientForm = messageClientForm;
    }

    @Override
    public void execute(String message) {
        /* message를 수신하면 MessageRecvObserver에 의해서 호출됩니다.
            - messageClientForm.getMessageArea()에 message를 추가합니다.
            - message 추가 후 개행 문자를 추가로 삽입 합니다. System.lineSeparator()
         */
        messageClientForm.getMessageArea().append(message);
        messageClientForm.getMessageArea().append("\n");
    }
}
