import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Scanner;
import java.util.UUID;

import com.nhnacademy.NotLoggedInException;

/**
 * ClientHandler 클래스는 클라이언트와의 통신을 담당하는 스레드입니다.
 */
public class ClientHandler extends Thread {

    String clientId;
    String userId;

    Socket socket;
    Scanner socketIn;
    PrintStream socketOut;

    static Map<String, ClientHandler> handlerMap = new HashMap<>();

    /**
     * ClientHandler 생성자는 소켓을 받아서 초기화합니다.
     *
     * @param socket 클라이언트와의 소켓
     * @throws IOException 소켓 생성에 실패한 경우
     */
    public ClientHandler(Socket socket) throws IOException {
        if (socket == null) {
            throw new IOException();
        }

        this.clientId = UUID.randomUUID().toString();
        this.userId = "";
        this.socket = socket;
        this.socketIn = new Scanner(socket.getInputStream());
        this.socketOut = new PrintStream(socket.getOutputStream());
    }


    public String getClientId() {
        return clientId;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public ClientHandler getClientHandler(String userId) {
        return handlerMap.get(userId);
    }

    public boolean isLoggedIn() {
        if (handlerMap.containsKey(this.userId)) {
            return true;
        }

        return false;
    }
    public boolean isLoggedIn(String userId) {
        System.out.println("생성할id:" + userId + " " + "현재쓰레드유저id:" + this.userId);

        if (handlerMap.containsKey(userId)) {
            return true;
        }
        
        return false;
    }

    public void send(String message) {
        this.socketOut.println(message);
    }

    public void send(String targetId, String message) {
        System.out.println(targetId + "에게 전송 메시지:" + message);
        getClientHandler(targetId).socketOut.println(message);
    }

    public void sendAll(String message) {
        for (Map.Entry<String, ClientHandler> entry : handlerMap.entrySet()) {
            if (this.userId != entry.getKey()) {
                entry.getValue().send(message);
            }
        }
    }


    @Override
    public void run() {
        try {
            while (true) {
                String line = this.socketIn.nextLine();
    
                // 공백 검증
                if (line.isEmpty()) {
                    System.out.printf("%s 회원이 공백을 입력하여 서버 연결을 종료합니다.\n", userId);
                    break;
                }
    

                // 로그인 로직
                if (line.startsWith("@login")) {
                    String[] splitLine = line.split(" ");
                    String createUserId = splitLine[1];
    
                    // 다른 회원의 로그인 ID 중복시
                    if (this.isLoggedIn(createUserId)) {
                        this.socketOut.println("동일한 id가 존재합니다. 다시 시도해 주세요");
                        continue;
                    }
    
                    // 기존 로그인 ID가 있었을 경우
                    if (!this.userId.isBlank()) {
                        handlerMap.remove(this.userId);
                    }
    
                    handlerMap.put(createUserId, this);
                    setUserId(createUserId);
    
                    this.socketOut.println("로그인 성공");
                    continue;
                }
    
                // 로그인 상태 검증 예외 처리
                try {
                    if (!this.isLoggedIn()) {
                        throw new NotLoggedInException("로그인 먼저 해주세요");
                    }
                } catch (NotLoggedInException e) {
                    this.socketOut.println(e.getMessage());
                    continue;
                }
                

                // 회원 목록 로직
                if (line.startsWith("@list")) {
                    for (Map.Entry<String, ClientHandler> entry : handlerMap.entrySet()) {
                        this.socketOut.println(entry.getKey());
                    }
                    continue;
                }
    

                // 개인 채팅 로직
                if (line.startsWith("@")) {
                    String[] splitLine = line.split(" ");
                    String message = splitLine[1];
    
                    String targetId = "";
                    for (int i = 1; i < splitLine[0].length(); i++) {
                        targetId += splitLine[0].charAt(i);
                    }
    
                    send(targetId, message);
                    continue;
                }
    

                // 전체 채팅 로직
                System.out.printf("%s 회원이 메시지를 전체에게 전송합니다.\n", userId);
                sendAll(line);
            }
        } finally {
            // 자원 해제
            try {
                handlerMap.remove(this.userId);
                this.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
