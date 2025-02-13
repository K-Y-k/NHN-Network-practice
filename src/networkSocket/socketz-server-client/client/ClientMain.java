import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class ClientMain {
    public static void main(String[] args) {
        // MessageClient 객체를 생성하고, 스레드를 이용해 동작 시킵니다.
        MessageClient messageClient = new MessageClient();
        Thread thread = new Thread(messageClient);
        thread.start();
    }
}
