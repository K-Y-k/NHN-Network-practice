package com.nhnacademy.smqtt.message;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import lombok.extern.slf4j.Slf4j;

/**
 * CONNACK 클래스는 MQTT 프로토콜의 CONNACK 메시지를 나타냅니다.
 * 이 메시지는 클라이언트가 CONNECT 메시지를 전송한 후 브로커로부터 수신하는 응답 메시지입니다.
 * 이 클래스는 sessionPresent 플래그와 returnCode를 포함합니다.
 */
public class CONNACK extends Message {
    private final boolean sessionPresent;
    private final int returnCode;

    /**
     * CONNACK 메시지를 직접 생성하는 생성자.
     *
     * @param sessionPresent 세션이 존재하는지 여부를 나타내는 플래그
     * @param returnCode     연결 결과를 나타내는 코드 (0~5)
     * @throws IllegalArgumentException returnCode가 0~5 범위를 벗어날 경우
     */
    public CONNACK(boolean sessionPresent, int returnCode) {
        super(Type.CONNACK);

        if (returnCode < 0 || returnCode > 5) {
            throw new IllegalArgumentException("Return Code는 0~5 사이여야 합니다.");
        }
        this.sessionPresent = sessionPresent;
        this.returnCode = returnCode;
    }

    /**
     * 바이트 배열을 받아서 CONNACK 객체를 생성하는 생성자.
     *
     * @param payload 바이트 배열로 표현된 CONNACK 메시지
     * @param offset  바이트 배열에서 메시지가 시작되는 위치
     * @param length  메시지의 길이 (항상 2)
     * @throws IllegalArgumentException payload가 null이거나, offset이 음수이거나,
     *                                  length가 2가 아니거나, payload의 길이가 offset + length보다 작을 경우
     */
    public CONNACK(byte[] payload, Integer offset, Integer length) {
        super(Type.CONNACK);

        if ((payload == null) || (offset < 0) || (length != 2) || (payload.length < offset + length)) {
            throw new IllegalArgumentException("CONNACK 메시지는 4바이트여야 합니다.");
        }

        ByteBuffer buffer = ByteBuffer.wrap(payload, offset, length);

        // 1. Session Present 플래그 확인
        byte byteValue = buffer.get();
        if ((byteValue & 0xFE) != 0x00) {
            throw new IllegalArgumentException("Session Present 값이 0 또는 1이어야 합니다.");
        }
        sessionPresent = (byteValue == 0x01);

        // 2. Return Code 확인
        byteValue = buffer.get();
        if (byteValue < 0 || byteValue > 5) {
            throw new IllegalArgumentException("Return Code 값이 올바르지 않습니다. (0~5)");
        }
        returnCode = byteValue;
    }

    /**
     * 바이트 배열을 받아서 CONNACK 객체를 생성하는 생성자.
     * 기본적으로 offset은 0, length는 payload의 길이로 설정됩니다.
     *
     * @param payload 바이트 배열로 표현된 CONNACK 메시지
     */
    public CONNACK(byte[] payload) {
        this(payload, 0, (payload != null) ? payload.length : 0);
    }

    /**
     * 세션이 존재하는지 여부를 반환합니다.
     *
     * @return 세션이 존재하면 true, 그렇지 않으면 false
     */
    public boolean isSessionPresent() {
        return sessionPresent;
    }

    /**
     * 연결 결과를 나타내는 Return Code를 반환합니다.
     *
     * @return Return Code (0~5)
     */
    public int getReturnCode() {
        return returnCode;
    }

    /**
     * 현재 객체를 MQTT CONNACK 메시지 형식의 바이트 배열로 변환합니다.
     *
     * @return MQTT CONNACK 메시지 형식의 바이트 배열
     */
    @Override
    public byte[] toByteArray() {
        ByteBuffer buffer = getByteBuffer(2);

        // TODO: 패킷을 구성하세요.
        if (this.sessionPresent) {
            buffer.put((byte) 1);
        } else {
            buffer.put((byte) 0);
        }

        buffer.put((byte) this.returnCode);

        return buffer.array();
    }
}
