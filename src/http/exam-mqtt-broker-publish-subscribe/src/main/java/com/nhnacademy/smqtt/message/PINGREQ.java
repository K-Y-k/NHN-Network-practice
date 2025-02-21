package com.nhnacademy.smqtt.message;

/**
 * PINGREQ 클래스는 MQTT 프로토콜의 PINGREQ 메시지를 나타냅니다.
 * 이 메시지는 클라이언트가 브로커에게 연결이 활성 상태임을 알리기 위해 전송됩니다.
 */
public class PINGREQ extends Message {
    /**
     * 기본 생성자.
     * PINGREQ 메시지를 생성합니다.
     */
    public PINGREQ() {
        super(Type.PINGREQ);
    }

    /**
     * 바이트 배열을 받아서 PINGREQ 객체를 생성하는 생성자.
     *
     * @param payload 바이트 배열로 표현된 PINGREQ 메시지
     * @param offset  바이트 배열에서 메시지가 시작되는 위치
     * @param length  메시지의 길이 (항상 0이어야 함)
     * @throws IllegalArgumentException payload가 null이거나, offset이 음수이거나,
     *                                  length가 0이 아니거나, payload의 길이가 offset +
     *                                  length보다 작을 경우
     */
    public PINGREQ(byte[] payload, Integer offset, Integer length) {
        super(Type.PINGREQ);

        if ((payload == null) || (offset < 0) || (length != 0) || (payload.length < offset + length)) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * 바이트 배열을 받아서 PINGREQ 객체를 생성하는 생성자.
     * 기본적으로 offset은 0, length는 payload의 길이로 설정됩니다.
     *
     * @param payload 바이트 배열로 표현된 PINGREQ 메시지
     */
    public PINGREQ(byte[] payload) {
        this(payload, 0, (payload != null) ? payload.length : 0);
    }
}
