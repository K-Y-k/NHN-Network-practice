import lombok.val;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.awt.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

@Slf4j
public class PortResponse implements Response {
    private final static String METHOD = "port";

    @Override
    public String getMethod() {
        return METHOD;
    }

    @Override
    public String execute(String value) {
        /* OS로부터 오픈되어 있는 Prot를 조회후 반환합니다.
         - value(port) 에 값이 존재 하지 않는다면 열려있는 모든 port를 반환합니다.
         - value(port) 값이 존재 한다면 해당 port에 해당되는 값을 반환합니다.
         - 다음과 같은 형식으로 반환 됩니다.
        TCP *:49742
        TCP *:49742
        TCP *:7000
        TCP *:7000
        TCP *:5000
        TCP *:5000
        TCP *:7797
        TCP *:7797
        TCP *:55920
        TCP 127.0.0.1:16105
        TCP 127.0.0.1:16115
        TCP 127.0.0.1:16107
        TCP 127.0.0.1:16117
        TCP *:19875
        TCP 127.0.0.1:19876
        TCP 127.0.0.1:63342
        TCP 127.0.0.1:52304
        TCP *:8888
        TCP 127.0.0.1:64120
        TCP 127.0.0.1:3376
        TCP 127.0.0.1:62451
        TCP 127.0.0.1:64913
        */

        StringBuilder sb = new StringBuilder();

        Process process;
        try {
            // -n은 숫자 형식, -i는 네트워크 연결을 표시, -P는 포트 번호를 표시
            // 이 명령어는 현재 열린 모든 네트워크 포트와 그에 해당하는 프로세스를 출력합니다.
            // Runtime.getRuntime().exec()은 자바의 기능으로 쉘 기능인 | 와 같은 파이프라인을 활용할 수 없다!
            process = Runtime.getRuntime().exec("lsof -i -n -P");

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                // 대기 상태인 포트만 조회
                if (line.contains("LISTEN")) {
                    // "\\s"은 공백문자(스페이스,탭,줄바꿈), "+"은 하나이상으로 
                    // 하나이상의 공백을 기준으로 분류
                    String[] lineSplit = line.split("\\s+");
                    System.out.println(Arrays.toString(lineSplit));

                    String protocol = lineSplit[7];
                    String port = lineSplit[8];
                    String result = protocol + " " + port + "\n";
                    System.out.println(result);

                    if (StringUtils.isEmpty(value)) {
                        sb.append(result);
                    } else if( StringUtils.isNotEmpty(value) && port.contains(value)){
                        sb.append(result);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    
        return sb.toString();
    }
}

class Test {
    public static void main(String[] args) {
        PortResponse pResponse = new PortResponse();

        pResponse.execute("xx");
    }
}