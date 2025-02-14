import java.io.PrintWriter;

public class SendMessageAction implements MessageAction {
    public SendMessageAction(PrintWriter printWriter) {
        // message 전송하기 위해서 printWriter를 사용 합니다. 초기화 해주세요
        this.printWriter = printWriter;
    }

    private final PrintWriter printWriter;

    @Override
    public void execute(String message) {
        // printWriter를 이용해서 client ->  server로 message를 전송 합니다.
        printWriter.println(message);
        
        // 여기서는 전달 받은 pritWiter에 true로 오토플러시를 설정했으므로 생략 가능 
        // printWriter.flush();
    }
}
