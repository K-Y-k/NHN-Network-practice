import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class MethodParserTest {
    static Stream<Arguments> methodValuePairs() {
        return Stream.of(
                Arguments.of("echo hello", "echo", "hello"),
                Arguments.of("echo nhnacademy", "echo", "nhnacademy"),
                Arguments.of("echo", "echo", ""),
                Arguments.of("echo 엔에이치엔아카데미", "echo", "엔에이치엔아카데미"));
    }

    @ParameterizedTest
    @MethodSource("methodValuePairs")
    void parseTest(String message, String method, String value) {
        MethodParser.MethodAndValue methodAndValue = MethodParser.parse(message);
        log.debug("message:{}, method:{}, value:{}", message, method, value);

        Assertions.assertAll(
            // 위 로그를 참고하여 method, value를 검증하는 코드를 작성하세요
            () -> {
                Assertions.assertEquals(value, methodAndValue.getValue());
            },
            () -> {
                Assertions.assertEquals(method, methodAndValue.getMethod());
            }
        );
    }


    static Stream<Arguments> emptyMessages() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of("    "));
    }

    @ParameterizedTest
    @MethodSource("emptyMessages")
    void parseByEmptyMessageTest(String message) {
        MethodParser.MethodAndValue methodAndValue = MethodParser.parse(message);
        Assertions.assertNull(methodAndValue);
    }
}