import org.apache.commons.lang3.StringUtils;
import lombok.val;
import java.util.Objects;

public class MethodParser {

    public static MethodAndValue parse(String message) {

        /*
         * client로 부터 전송되는 message를 method / value로 분리합니다.
         * - ex)echo hello -> method : echo , value : hello
         * - ex)echo -> method : echo , value:""
         * - message "" or null 이면 null을 반환합니다.
         * - 파싱한 결과는 MethodAndValue로 반환합니다.
         */

        if (StringUtils.isEmpty(message)) {
            return null;
        }

        String[] messages = message.split(" ");

        if (messages.length > 0) {
            if (messages.length == 1) {
                return new MethodAndValue(messages[0], "");
            }

            return new MethodAndValue(messages[0], messages[1]);
        }
        
        return null;
    }

    public static class MethodAndValue {
        private final String method;
        private final String value;

        public MethodAndValue(String method, String value) {
            // 초기화 합니다.
            this.method = method;
            this.value = value;
        }

        public String getMethod() {
            // method 반환합니니다.
            return this.method;
        }

        public String getValue() {
            // value 반환합니니다.
            return this.value;
        }
    }
}