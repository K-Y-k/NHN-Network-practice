import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EchoResponseTest {
    Response echoResponse;

    @BeforeEach
    void setUp() {
        echoResponse = new EchoResponse();
    }

    @Test
    @DisplayName("get method")
    void getResponse() {
        // echoResponse.getMethod()를 호출 했을 때 echo가 반환되는지 검증합니다.
        Assertions.assertEquals("echo", echoResponse.getMethod());
    }

    @Test
    @DisplayName("validate echoResponse, echo method에 대한 응답을 구현한 구현체인지 검증 합니다.")
    void validate() {
        // echoResponse가 echo method에 대한 응답객체인지 검증합니다.
        Assertions.assertTrue(echoResponse.validate("echo"));
    }

    @Test
    @DisplayName("execute")
    void execute() {
        // actual 값이 hello인증 검증 합니다. 
        // 즉 echo Server 임으로 client로 부터 전달받은 message를 client에게 반환합니다.
        String actual = echoResponse.execute("hello");
        
        Assertions.assertEquals("hello", actual);

    }

}