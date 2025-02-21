package com.nhnacademy.smqtt.message;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class CONNACKTest {

    /**
     * 정상적인 바이트 배열을 사용하여 객체 생성 테스트
     *
     * 이 테스트는 주어진 바이트 배열을 사용하여 CONNACK 객체를 생성하고,
     * sessionPresent와 returnCode가 예상대로 설정되는지 확인합니다.
     */
    @Test
    void testValidByteArrayCreation() {
        byte[] payload = { 0x01, 0x02 }; // sessionPresent = true, returnCode = 2
        CONNACK connack = new CONNACK(payload);

        assertTrue(connack.isSessionPresent());
        assertEquals(2, connack.getReturnCode());
    }

    /**
     * 너무 짧거나 잘못된 바이트 배열 입력 시 예외 발생
     *
     * 이 테스트는 CONNACK 객체 생성 시 너무 짧거나 잘못된 길이의 바이트 배열이 입력될 경우
     * IllegalArgumentException이 발생하는지 확인합니다.
     */
    @Test
    void testInvalidByteArrayLength() {
        byte[] shortPayload = { 0x01 }; // 1바이트만 존재 -> 예외 발생
        assertThrows(IllegalArgumentException.class, () -> new CONNACK(shortPayload));

        byte[] emptyPayload = {}; // 길이가 0
        assertThrows(IllegalArgumentException.class, () -> new CONNACK(emptyPayload));

        byte[] invalidPayload = { 0x01, 0x02, 0x03 }; // 길이가 3 -> 예외 발생
        assertThrows(IllegalArgumentException.class, () -> new CONNACK(invalidPayload));
    }

    /**
     * 잘못된 Session Present 값이 포함된 바이트 배열 테스트
     *
     * 이 테스트는 sessionPresent 값이 잘못된 바이트 배열이 입력될 경우
     * IllegalArgumentException이 발생하는지 확인합니다.
     */
    @Test
    void testInvalidSessionPresentValue() {
        byte[] invalidPayload = { 0x02, 0x01 }; // sessionPresent 값이 0x02 (잘못된 값)
        assertThrows(IllegalArgumentException.class, () -> new CONNACK(invalidPayload));
    }

    /**
     * 잘못된 Return Code 값이 포함된 바이트 배열 테스트
     *
     * 이 테스트는 returnCode 값이 잘못된 바이트 배열이 입력될 경우
     * IllegalArgumentException이 발생하는지 확인합니다.
     */
    @Test
    void testInvalidReturnCodeValue() {
        byte[] invalidPayload1 = { 0x00, 0x06 }; // returnCode 값이 6 (잘못된 값)
        byte[] invalidPayload2 = { 0x00, (byte) 0xFF }; // returnCode 값이 음수

        assertThrows(IllegalArgumentException.class, () -> new CONNACK(invalidPayload1));
        assertThrows(IllegalArgumentException.class, () -> new CONNACK(invalidPayload2));
    }

    /**
     * 객체를 직렬화(`toByteArray()`) 후 다시 객체로 변환하여 데이터 유지 여부 확인
     *
     * 이 테스트는 CONNACK 객체를 직렬화한 후 다시 역직렬화하여 
     * 원본 객체의 데이터가 유지되는지 확인합니다.
     */
    @Test
    void testSerializationAndDeserialization() {
        CONNACK original = new CONNACK(true, 3);
        byte[] serialized = original.toByteArray();
        CONNACK deserialized = new CONNACK(serialized, 2, 2);

        assertEquals(original.isSessionPresent(), deserialized.isSessionPresent());
        assertEquals(original.getReturnCode(), deserialized.getReturnCode());
    }

    /**
     * offset과 length를 사용하는 생성자 테스트 (부분 데이터만 읽기)
     *
     * 이 테스트는 주어진 바이트 배열의 특정 부분을 사용하여 CONNACK 객체를 생성하고,
     * 해당 부분의 데이터가 올바르게 해석되는지 확인합니다.
     */
    @Test
    void testValidByteArrayWithOffsetAndLength() {
        byte[] fullPayload = { 0x55, 0x44, 0x01, 0x03, 0x33, 0x66 }; // 중간 {0x01, 0x03}만 CONNACK
        CONNACK connack = new CONNACK(fullPayload, 2, 2);

        assertTrue(connack.isSessionPresent());
        assertEquals(3, connack.getReturnCode());
    }

    /**
     * 잘못된 offset과 length 조합 테스트
     *
     * 이 테스트는 CONNACK 객체 생성 시 잘못된 offset과 length 조합이 입력될 경우
     * IllegalArgumentException이 발생하는지 확인합니다.
     */
    @Test
    void testInvalidOffsetAndLength() {
        byte[] fullPayload = { 0x01, 0x03, 0x44, 0x55 };

        assertThrows(IllegalArgumentException.class, () -> new CONNACK(fullPayload, -1, 2)); // 음수 offset
        assertThrows(IllegalArgumentException.class, () -> new CONNACK(fullPayload, 0, 5)); // length 초과
        assertThrows(IllegalArgumentException.class, () -> new CONNACK(fullPayload, 3, 2)); // offset + length 초과
    }
}
