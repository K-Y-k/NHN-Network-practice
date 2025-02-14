import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class WhisperResponse implements Response {
    @Override
    public String getMethod() {
        // method = "whisper" 반환합니다.
        return "whisper";
    }

    @Override
    public String execute(String value) {
        // whisper marco hello 형태로 호출됩니다.
        // method는 whisper, value는 marco hello <-- 입니다.

        // 로그인이 되어있지 않다면 "login required!" 반환합니다.
        if (!Session.isLogin()) {
            return "login required!";
        }

        // value null or "" 이면 "empty message!" 반환합니다.
        if (StringUtils.isEmpty(value)) {
            return "empty message!";
        }

        /* value 형식이
            {clientId} {message} 아니라면 "empty message!" 반환합니다.
            - marco hello (O)
            - marco (X)
            -"" (X)
            - marco nice to meet you (O)
         */
        String[] valueSplit = value.split(" ");
        if (valueSplit[0].isEmpty() || valueSplit[1].isEmpty()) {
            return "empty message!";
        }

        // value가 marco hello 라면 marco아이디를 사용하는 cleint에게 hello message를 응답합니다.
        Socket clientSocket = MessageServer.getClientSocket(valueSplit[0]);
        try {
            PrintWriter clientOut = new PrintWriter(clientSocket.getOutputStream());

            clientOut.println(String.format("[%s] %s", Session.getCurrentId(), valueSplit[1]));
            clientOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 메시지 전송이 완료되면, "[whisper][marco]" hello 형태로 반환합니다.
        return String.format("[%s][%s] %s", getMethod(), valueSplit[0], valueSplit[1]);
    }
}
