package com.nhnacademy.smqtt.message;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class SUBSCRIBETest {
    private static final String TOPIC = "test/topic";

    /**
     * topic을 직접 받는 생성자 테스트
     *
     * 이 테스트는 주어진 topic을 사용하여 SUBSCRIBE 객체를 생성하고,
     * 객체가 정상적으로 생성되었는지와 topic 및 기본 packetId가 올바르게 설정되었는지 확인합니다.
     */
    @Test
    void testValidSubscribeCreation() {
        assertDoesNotThrow(() -> {
            SUBSCRIBE subscribe = new SUBSCRIBE(TOPIC);
            assertNotNull(subscribe);
            assertEquals(TOPIC, subscribe.getTopic());
            assertEquals(0, subscribe.getPacketId()); // 기본 값
        });
    }

    /**
     * topic이 null 또는 빈 문자열일 경우 예외 발생 테스트
     *
     * 이 테스트는 null 또는 빈 문자열을 topic으로 사용하여 SUBSCRIBE 객체를 생성할 때
     * IllegalArgumentException이 발생하는지 확인합니다.
     */
    @Test
    void testInvalidSubscribeCreation() {
        assertThrows(IllegalArgumentException.class, () -> new SUBSCRIBE((String) null));
        assertThrows(IllegalArgumentException.class, () -> new SUBSCRIBE(""));
    }

    /**
     * 바이트 배열을 이용한 생성자 테스트 (정상적인 경우)
     *
     * 이 테스트는 유효한 바이트 배열을 사용하여 SUBSCRIBE 객체를 생성하고,
     * 객체가 정상적으로 생성되었는지와 packetId, topic, QoS가 올바르게 설정되었는지 확인합니다.
     */
    @Test
    void testSubscribeCreationFromValidByteArray() {
        String topic = TOPIC;
        byte[] topicBytes = topic.getBytes(StandardCharsets.UTF_8);
        int packetId = 1234;
        byte qos = 1;

        ByteBuffer buffer = ByteBuffer.allocate(2 + 2 + topicBytes.length + 1);
        buffer.putShort((short) packetId);
        buffer.putShort((short) topicBytes.length);
        buffer.put(topicBytes);
        buffer.put(qos);
        byte[] payload = buffer.array();

        SUBSCRIBE subscribe = new SUBSCRIBE(payload);

        assertNotNull(subscribe);
        assertEquals(packetId, subscribe.getPacketId());
        assertEquals(topic, subscribe.getTopic());
        assertEquals(qos, subscribe.getQoS());
    }

    /**
     * 바이트 배열이 잘못된 경우 예외 발생 테스트
     *
     * 이 테스트는 잘못된 바이트 배열을 사용하여 SUBSCRIBE 객체를 생성할 때
     * IllegalArgumentException이 발생하는지 확인합니다.
     */
    @Test
    void testSubscribeCreationFromInvalidByteArray() {
        byte[] invalidPayload1 = null;        // Null payload
        byte[] invalidPayload2 = new byte[2]; // 너무 짧은 길이
        byte[] invalidPayload3 = new byte[] { 0x00, 0x01, 0x00 }; // 잘못된 형식

        assertThrows(IllegalArgumentException.class, () -> new SUBSCRIBE(invalidPayload1));
        assertThrows(IllegalArgumentException.class, () -> new SUBSCRIBE(invalidPayload2));
        assertThrows(IllegalArgumentException.class, () -> new SUBSCRIBE(invalidPayload3));
    }

    /**
     * toByteArray()로 변환 후 원래 값과 동일한지 확인
     *
     * 이 테스트는 SUBSCRIBE 객체를 바이트 배열로 직렬화한 후 다시 역직렬화하여
     * 원본 객체의 데이터가 유지되는지 확인합니다.
     */
    @Test
    void testToByteArray() {
        String topic = TOPIC;
        SUBSCRIBE original = new SUBSCRIBE(topic);

        byte[] byteArray = original.toByteArray();
        assertNotNull(byteArray);

        SUBSCRIBE reconstructed = new SUBSCRIBE(byteArray, 2, byteArray.length - 2);
        assertEquals(original.getTopic(), reconstructed.getTopic());
    }
}
