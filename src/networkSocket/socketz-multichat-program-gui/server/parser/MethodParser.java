public class MethodParser {

    public static MethodAndValue parse(String message){
        String messages[] = message.split(" ");
        if(messages.length > 0) {
            // 메소드만 있는 경우 ex) "echo"
            if (messages.length == 1) {
                return new MethodAndValue(messages[0], "");
            }

            // 메소드 전달대상자 메시지가 있는 경우 ex) "whisper marco hello"
            if (messages.length == 3) {
                // 첫번째부터 messages[0].length()까지 잘라냄 -> "marco hello"
                String value = message.substring(messages[0].length());
                return new MethodAndValue(messages[0], value.trim());
            }

            return new MethodAndValue(messages[0], messages[1]);
        }
        return null;
    }

    public static class MethodAndValue{
        private final String method;
        private final String value;

        public MethodAndValue(String method, String value) {
            this.method = method;
            this.value = value;
        }

        public String getMethod() {
            return method;
        }

        public String getValue() {
            return value;
        }
    }
}