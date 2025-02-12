public class EchoResponse implements Response {
    // echo method에 해당되는 응답을 구현 합니다.

    private static final String METHOD = "echo";

    @Override
    public String getMethod() {
        // EchoResponse의 METHOD 를 반환 합니다.
        return METHOD;
    }

    @Override
    public String execute(String value) {
        /*
         * value를 반환 합니다.
         * - ex) echo hello message -> method = echo , value = hello 임으로 value를 반환 합니다.
         */
        return value;
    }
}
