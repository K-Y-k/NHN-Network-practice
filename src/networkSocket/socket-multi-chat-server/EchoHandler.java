import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.security.KeyStore.Entry;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;

/**
 * EchoHandler는 클라이언트와 서버 간의 통신을 처리하는 스레드입니다.
 *
 * @author NHN Academy Corp.
 */
public class EchoHandler extends Thread {
    /**
     * 모든 EchoHandler 인스턴스를 저장하는 정적 맵입니다.
     * 키는 스레드 ID, 값은 EchoHandler 인스턴스입니다.
     */
    static Map<Long, EchoHandler> handlerMap = new HashMap<>();
    Socket socket;
    Scanner socketIn;
    PrintStream socketOut;

    /**
     * EchoHandler를 생성합니다.
     *
     * @param socket 클라이언트와의 연결을 나타내는 소켓입니다.
     * @throws IOException 소켓 입력/출력 스트림을 생성하는 데 실패하면 발생합니다.
     */
    public EchoHandler(Socket socket) throws IOException {
        if (socket == null) {
            throw new IOException();
        }

        this.socket = socket;
        this.socketIn = new Scanner(socket.getInputStream());
        this.socketOut = new PrintStream(socket.getOutputStream());
    }

    /**
     * 클라이언트와의 통신을 처리하는 메소드입니다.
     * 클라이언트와의 연결을 맺고, 클라이언트가 전송하는 메시지를 수신하여 처리합니다.
     *
     * - 구현 과정
     * 클라이언트와의 연결을 맺고, EchoHandler 인스턴스를 handlerMap에 등록합니다.
     * 클라이언트가 전송하는 메시지를 수신하여 처리합니다.
     * 수신한 메시지를 모든 클라이언트에게 전달합니다.
     * 클라이언트가 연결을 끊을 때까지 위 과정을 반복합니다.
     * 클라이언트가 연결을 끊으면, EchoHandler 인스턴스를 handlerMap에서 제거하고 소켓을 닫습니다.
     */
    @Override
    public void run() {
        handlerMap.put(Thread.currentThread().threadId()), this);
        
        while (!Thread.currentThread().isInterrupted()) {
            String line = socketIn.nextLine();

            if (line.isEmpty()) {
                System.out.printf("%d 회원이공백을 입력하여 서버와의 연결을 종료합니다.\n", Thread.currentThread().threadId());
                break;
            }

            System.out.printf("%d 회원이 메시지를 전송합니다.\n", Thread.currentThread().threadId());
            broadcast(line);
        }

        handlerMap.remove(Thread.currentThread().threadId());

        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 메시지를 전송한 클라이언트의 ID를 포함해 메시지를 전송합니다.
     *
     * @param id 메시지를 전송한 클라이언트의 ID입니다.
     * @param message 전송할 메시지입니다.
     */
    public void sendToOtherClients(long id, String message) {
        this.socketOut.printf("%d: %s\n", id, message);
    }

    /**
     * 모든 클라이언트에게 메시지를 전송합니다.
     * 단, 메시지를 보낸 클라이언트는 제외합니다.
     *
     * @param message 방송할 메시지입니다.
     */
    static void broadcast(String message) {
        for (Map.Entry<Long, EchoHandler> entry : handlerMap.entrySet()) {
            if (entry.getKey() != Thread.currentThread().threadId()) {
                EchoHandler currHandler = entry.getValue();
                currHandler.sendToOtherClients(Thread.currentThread().threadId(), message);
            }
        }
    }
}
