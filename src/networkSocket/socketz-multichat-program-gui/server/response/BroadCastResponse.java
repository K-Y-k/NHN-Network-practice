import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

@Slf4j
public class BroadCastResponse implements Response {
    @Override
    public String getMethod() {
        // method = "broadcast" 설정합니다.
        return "broadcast";
    }

    @Override
    public String execute(String value) {

        /* TODO#1-2 MessageServer.getClientIds() 해당되는 모든 client에게 message를 전송합니다.
            - PrintWriter를 사용해서 각 client에게 응답합니다.
            - 응답시 sendCount가 증가됩니다.
            - value 값은 client에게 전송할 message입니다.
         */
        List<String> ids = MessageServer.getClientIds();
        int sendCount = 0;

        for (String id : ids) {
            // 현재 클라이언트id의 소켓 가져오기
            Socket clientSocket = MessageServer.getClientSocket(id);
            
            // 클라이언트의 소켓 연결이 종료되었거나 유효하지 않은 상태인 경우
            if (clientSocket.isClosed()) {
                // 로그인된 상태라면 지워준다.
                if (Session.isLogin()) {
                    MessageServer.removeClient(Session.getCurrentId());
                }

                continue;
            }


            try {
                // 이유1.PrintWriter가 종료할 때 인자로 전달 받은 클라이언트 소켓도 같이 종료하는데
                //       종료되기 전에 빈 버퍼를 flush한다.
                //       빈 버퍼를 MethodJob으로 return 받아서 read로 읽어올 때 null로 판단되어
                //       finally 구문으로 넘어가져서 프로그램의 동작이 끝이나서 현재 클라이언트에서의 프로그램 실행이 불가능한 상태가 된다.

                // 이유2.IO 스트림 클래스가 종료될 때 인자로 전달 받은 소켓도 같이 종료된다.
                //       MethodJob에서도 같은 소켓을 공유하기 때문에 MethodJob에서의 BufferedReader와 PritWriter도 같이 종료된다.

                // 이러한 이유로 try-with-resources를 상황에 따라 적절히 사용해야 한다.
                PrintWriter clientOut = new PrintWriter(clientSocket.getOutputStream());

                clientOut.println(value);
                clientOut.flush();

                sendCount++;
            } catch (IOException e) {
                throw new RuntimeException();
            }
        }


        return String.format("{%d}명에게 \"{%S}\"를 전송 했습니다.", sendCount, value);
    }
}