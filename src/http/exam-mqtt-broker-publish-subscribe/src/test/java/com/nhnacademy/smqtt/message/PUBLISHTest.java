package com.nhnacademy.smqtt.message;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class PUBLISHTest {
    static final String TOPIC = "test/topic";
    static final String MESSAGE = "Hello MQTT!";

    /**
     * topic과 message를 직접 받는 생성자 테스트
     *
     * 이 테스트는 주어진 topic과 message를 사용하여 PUBLISH 객체를 생성하고,
     * 객체가 정상적으로 생성되었는지와 topic 및 message가 올바르게 설정되었는지 확인합니다.
     */
    @Test
    void testValidPublishCreation() {
        PUBLISH publish = new PUBLISH(TOPIC, MESSAGE);
        assertNotNull(publish);
        assertEquals(TOPIC, publish.getTopic());
        assertEquals(MESSAGE, publish.getMessage());
    }

    /**
     * topic이 null 또는 빈 문자열일 경우 예외 발생 테스트
     *
     * 이 테스트는 null 또는 빈 문자열을 topic으로 사용하여 PUBLISH 객체를 생성할 때
     * IllegalArgumentException이 발생하는지 확인합니다.
     */
    @ParameterizedTest
    @MethodSource("testInvalidPublishCreationParamProvider")
    void testInvalidPublishCreation(String topic, String message) {
        assertThrows(IllegalArgumentException.class, () -> new PUBLISH(topic, message));
    }

    static Stream<Arguments> testInvalidPublishCreationParamProvider() {
        return Stream.of(
                Arguments.of(null, MESSAGE),
                Arguments.of("", MESSAGE));
    }

    /**
     * 바이트 배열을 이용한 생성자 테스트 (정상적인 경우)
     *
     * 이 테스트는 유효한 바이트 배열을 사용하여 PUBLISH 객체를 생성하고,
     * 객체가 정상적으로 생성되었는지와 packetId, topic, message가 올바르게 설정되었는지 확인합니다.
     */
    @Test
    void testPublishCreationFromValidByteArray() {
        byte[] topicBytes = TOPIC.getBytes(StandardCharsets.UTF_8);
        byte[] messageBytes = MESSAGE.getBytes(StandardCharsets.UTF_8);
        int packetId = 1234;

        ByteBuffer buffer = ByteBuffer.allocate(2 + topicBytes.length + 2 + messageBytes.length);
        buffer.putShort((short) topicBytes.length);
        buffer.put(topicBytes);

        buffer.putShort((short) packetId);
        buffer.put(messageBytes);
        
        byte[] payload = buffer.array();

        PUBLISH publish = new PUBLISH(payload);

        assertNotNull(publish);
        assertEquals(TOPIC, publish.getTopic());
        assertEquals(MESSAGE, publish.getMessage());
        assertEquals(packetId, publish.getPacketId());
    }

    /**
     * 바이트 배열이 잘못된 경우 예외 발생 테스트
     *
     * 이 테스트는 잘못된 바이트 배열을 사용하여 PUBLISH 객체를 생성할 때
     * IllegalArgumentException이 발생하는지 확인합니다.
     */
    @ParameterizedTest()
    @MethodSource("testPublishCreationFromInvalidByteArrayParamProvider")
    void testPublishCreationFromInvalidByteArray(byte[] payload) {
        assertThrows(IllegalArgumentException.class, () -> new PUBLISH(payload));
    }

    static Stream<Arguments> testPublishCreationFromInvalidByteArrayParamProvider() {
        return Stream.of(
                Arguments.of((byte[]) null),
                Arguments.of(new byte[2]),
                Arguments.of(new byte[] { 0x00, 0x01, 0x00 })); // 잘못된 형식

    }

    /**
     * toByteArray()로 변환 후 원래 값과 동일한지 확인
     *
     * 이 테스트는 PUBLISH 객체를 바이트 배열로 직렬화한 후 다시 역직렬화하여
     * 원본 객체의 데이터가 유지되는지 확인합니다.
     */
    @Test
    void testToByteArray() {
        PUBLISH original = new PUBLISH(TOPIC, MESSAGE);

        byte[] byteArray = original.toByteArray();
        assertNotNull(byteArray);

        PUBLISH reconstructed = new PUBLISH(byteArray, 2, byteArray.length - 2);
        assertEquals(original.getTopic(), reconstructed.getTopic());
        assertEquals(original.getMessage(), reconstructed.getMessage());
    }
}
