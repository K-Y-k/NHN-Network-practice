package com.nhnacademy.smqtt.message;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class MessageTest {
    private static class TestMessage extends Message {
        TestMessage(Type type) {
            super(type);
        }

        @Override
        public byte[] toByteArray() {
            return getFixedHeader(0);
        }
    }

    private Message testMessage;

    /**
     * 각 테스트 전에 실행되어 Message 객체를 초기화합니다.
     */
    @BeforeEach
    void setUp() {
        testMessage = new TestMessage(Message.Type.CONNECT);
    }

    /**
     * 클래스가 추상 클래스로 정의되어 있는지 확인합니다.
     */
    @Test
    void testAbstractClass() {
        assertTrue(Modifier.isAbstract(Message.class.getModifiers()));
    }

    /**
     * 추상 클래스 생성자의 접근 제어자는 public으로 선언하지 않았는지 확인합니다.
     */
    @Test
    void testConstructorAccessModifier() {
        Constructor<?>[] constructors = Message.class.getDeclaredConstructors();

        for (Constructor<?> constructor : constructors) {
            assertFalse(Modifier.isPublic(constructor.getModifiers()));
        }
    }

    /**
     * 메시지 타입이 올바르게 설정되는지 테스트합니다.
     */
    @Test
    void testGetType() {
        assertEquals(Message.Type.CONNECT, testMessage.getType());
    }

    /**
     * 타입 값이 올바르게 검증되는지 테스트합니다.
     */
    @Test
    void testValidateTypeValue() {
        assertTrue(testMessage.validate(1)); // CONNECT = 1
        assertFalse(testMessage.validate(3)); // PUBLISH = 3
    }

    /**
     * 올바른 QoS 값이 설정되는지 테스트합니다.
     */
    @Test
    void testSetAndGetQoS() {
        testMessage.setQoS(1);
        assertEquals(1, testMessage.getQoS());

        testMessage.setQoS(2);
        assertEquals(2, testMessage.getQoS());
    }

    /**
     * 잘못된 QoS 값을 설정할 때 예외가 발생하는지 테스트합니다.
     */
    @Test
    void testSetInvalidQoS() {
        assertThrows(IllegalArgumentException.class, () -> testMessage.setQoS(-1));
        assertThrows(IllegalArgumentException.class, () -> testMessage.setQoS(3));
    }

    /**
     * 메시지가 올바르게 보존(Retained)되는지 테스트합니다.
     */
    @Test
    void testSetAndGetRetained() {
        testMessage.setRetained(true);
        assertTrue(testMessage.isRetained());

        testMessage.setRetained(false);
        assertFalse(testMessage.isRetained());
    }

    /**
     * 메시지가 올바르게 중복(Duplicated) 설정되는지 테스트합니다.
     */
    @Test
    void testSetAndGetDuplicated() {
        testMessage.setDuplicated(true);
        assertTrue(testMessage.isDuplicated());

        testMessage.setDuplicated(false);
        assertFalse(testMessage.isDuplicated());
    }

    /**
     * 고정 헤더가 올바르게 생성되는지 테스트합니다.
     */
    @Test
    void testGetFixedHeader() {
        byte[] header = testMessage.getFixedHeader(10);
        assertEquals(2, header.length);
        assertEquals(16, header[0]); // CONNECT = 1 << 4 = 16
        assertEquals(10, header[1]);
    }

    /**
     * ByteBuffer가 올바르게 생성되는지 테스트합니다.
     */
    @Test
    void testGetByteBuffer() {
        ByteBuffer buffer = testMessage.getByteBuffer(5);
        assertNotNull(buffer);
        assertEquals(7, buffer.capacity()); // 1 byte (fixed header) + 2 byte (remaining length) + 5 bytes

        buffer.flip();
        assertEquals(16, buffer.get()); // CONNECT (1 << 4)
    }

    /**
     * 메시지를 바이트 배열로 변환할 수 있는지 테스트합니다.
     */
    @Test
    void testToByteArray() {
        byte[] byteArray = testMessage.toByteArray();
        assertEquals(2, byteArray.length);
        assertEquals(16, byteArray[0]); // CONNECT
        assertEquals(0, byteArray[1]); // Length = 0
    }

    /**
     * 잘못된 길이로 ByteBuffer를 생성할 때 예외가 발생하는지 테스트합니다.
     */
    @Test
    void testGetByteBufferWithInvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> testMessage.getByteBuffer(268435456)); // 초과 길이
    }

    /**
     * 유효한 바이트 배열을 파싱할 수 있는지 테스트합니다.
     */
    @Test
    void testParsingValidMessage() {
        Message parsedMessage = Message.parsing(new CONNECT("ClientTest").toByteArray());
        assertEquals(Message.Type.CONNECT, parsedMessage.getType());
    }

    /**
     * 잘못된 바이트 배열을 파싱할 때 예외가 발생하는지 테스트합니다.
     */
    @ParameterizedTest
    @MethodSource("testParsingInvalidMessageParamProvider")
    void testParsingInvalidMessage(byte[] invalidMessage) {
        assertThrows(IllegalArgumentException.class, () -> Message.parsing(invalidMessage));
    }

    static Stream<Arguments> testParsingInvalidMessageParamProvider() {
        return Stream.of(
                Arguments.of((byte[]) null),
                Arguments.of(new byte[] { 0x7F, 0x00 }),
                Arguments.of(new byte[] { 0x10, (byte) 0x80 }));
    }

    /**
     * 바이트 배열을 오프셋과 길이를 포함하여 파싱할 수 있는지 테스트합니다.
     */
    @Test
    void testParsingWithOffsetAndLength() {
        CONNECT connect = new CONNECT("ClientTest");
        byte[] connectBytes = connect.toByteArray();
        byte[] buffer = new byte[connectBytes.length + 10];
        System.arraycopy(connectBytes, 0, buffer, 5, connectBytes.length);
        Message parsedMessage = Message.parsing(buffer, 5, connectBytes.length);
        assertEquals(Message.Type.CONNECT, parsedMessage.getType());
    }

    /**
     * 바이트 배열이 null일 때 예외가 발생하는지 테스트합니다.
     */
    @Test
    void testParsingNullMessage() {
        assertThrows(IllegalArgumentException.class, () -> Message.parsing(null));
    }

    /**
     * 메시지 타입을 문자열로 변환할 수 있는지 테스트합니다.
     */
    @Test
    void testToString() {
        assertEquals("CONNECT", testMessage.toString());
    }
}
