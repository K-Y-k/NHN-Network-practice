public interface Response {

    String getMethod();

    String execute(String value);

    default boolean validate(String method) {
        /*
         * Response interface를 구현한 구현체(EchoResponse)의 method가 
         * parameter로 전달되는 method와 일치하면 true를 반환합니다.
         * - method에 따라서 실행되어야 할 Response 구현체를 식별합니다.
         * - ex) method 'echo' 인 response를 검증하기 위해서는 echoResponse.validate("echo") == true
         * - getMethod()를 이용해서 구현하세요.
         */
        return getMethod().equals(method);
    }

}
