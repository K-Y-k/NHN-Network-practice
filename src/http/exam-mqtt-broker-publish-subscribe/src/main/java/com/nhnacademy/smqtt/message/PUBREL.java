package com.nhnacademy.smqtt.message;

import java.nio.ByteBuffer;

/**
 * PUBREL 클래스는 MQTT 프로토콜의 PUBREL 메시지를 나타냅니다.
 * 이 메시지는 QoS 2 수준의 메시지 전송에서 사용되며, 클라이언트가 PUBREC 메시지를 수신한 후
 * 브로커에게 메시지를 성공적으로 수신했음을 알리기 위해 전송됩니다.
 */
public class PUBREL extends Message {
    private final int packetId;

    /**
     * 주어진 패킷 식별자를 사용하여 PUBREL 메시지를 생성하는 생성자.
     *
     * @param packetId 패킷 식별자. 이 식별자는 PUBREL 메시지가 응답하는 PUBLISH 메시지의
     *                 식별자와 동일해야 합니다.
     */
    public PUBREL(int packetId) {
        super(Type.PUBREL);
        this.packetId = packetId;
    }

    /**
     * 바이트 배열을 받아서 PUBREL 객체를 생성하는 생성자.
     *
     * @param payload 바이트 배열로 표현된 PUBREL 메시지. 이 배열은 PUBREL 메시지의
     *                패킷 식별자를 포함해야 합니다.
     * @param offset  바이트 배열에서 메시지가 시작되는 위치. 일반적으로 0으로 설정됩니다.
     * @param length  메시지의 길이. PUBREL 메시지의 경우 항상 2이어야 합니다.
     * @throws IllegalArgumentException payload가 null이거나, offset이 음수이거나,
     *                                  length가 2가 아니거나, payload의 길이가 offset +
     *                                  length보다 작을 경우 발생합니다.
     */
    public PUBREL(byte[] payload, Integer offset, Integer length) {
        super(Type.PUBREL);

        if ((payload == null) || (offset < 0) || (length != 2) || (payload.length < offset + length)) {
            throw new IllegalArgumentException("PUBREL 메시지는 2바이트여야 합니다.");
        }

        ByteBuffer buffer = ByteBuffer.wrap(payload, offset, length);

        // 1. Packet Identifier 읽기
        this.packetId = buffer.getShort() & 0xFFFF;
    }

    /**
     * 바이트 배열을 받아서 PUBREL 객체를 생성하는 생성자.
     * 기본적으로 offset은 0, length는 payload의 길이로 설정됩니다.
     *
     * @param payload 바이트 배열로 표현된 PUBREL 메시지. 이 배열은 PUBREL 메시지의
     *                패킷 식별자를 포함해야 합니다.
     */
    public PUBREL(byte[] payload) {
        this(payload, 0, (payload != null) ? payload.length : 0);
    }

    /**
     * 패킷 식별자를 반환합니다.
     *
     * @return packetId 패킷 식별자. 이 식별자는 PUBREL 메시지가 응답하는 PUBLISH 메시지의
     *         식별자와 동일합니다.
     */
    public int getPacketId() {
        return packetId;
    }

    /**
     * 현재 객체를 MQTT PUBREL 메시지 형식의 바이트 배열로 변환합니다.
     * 이 메서드는 PUBREL 메시지를 전송하기 위해 사용됩니다.
     *
     * @return MQTT PUBREL 메시지 형식의 바이트 배열. 이 배열은 패킷 식별자를 포함합니다.
     */
    @Override
    public byte[] toByteArray() {
        ByteBuffer buffer = getByteBuffer(2);

        buffer.putShort((short) packetId);

        return buffer.array();
    }
}
