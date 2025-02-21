package com.nhnacademy.smqtt.message;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;

import com.nhnacademy.smqtt.client.Client;

import lombok.extern.slf4j.Slf4j;

/**
 * SUBSCRIBE 클래스는 MQTT 프로토콜의 SUBSCRIBE 메시지를 나타냅니다.
 * 이 메시지는 클라이언트가 브로커에게 특정 주제를 구독하기 위해 전송됩니다.
 * 주제는 클라이언트가 수신하고자 하는 메시지의 범위를 정의합니다.
 */
@Slf4j
public class SUBSCRIBE extends Message {
    private final int packetId;
    // TODO: topic을 추가하고 관련된 메서드를 수정하세요.
    private String topic;

    /**
     * 주어진 주제를 사용하여 SUBSCRIBE 메시지를 생성하는 생성자.
     *
     * @param topic 구독할 주제. 주제는 null이거나 비어 있을 수 없습니다.
     * @throws IllegalArgumentException 주제가 null이거나 비어 있는 경우 발생합니다.
     */
    public SUBSCRIBE(String topic) {
        super(Type.SUBSCRIBE);

        // TODO: 인수를 검증하세요.
        if (StringUtils.isEmpty(topic)) {
            throw new IllegalArgumentException("invalid : topic is null or empty");
        }

        this.topic = topic;
        this.packetId = Client.getPacketId();

        log.debug("생성자 초기화 topic:{}", this.topic);
        log.debug("생성자 초기화 packetId:{}", this.packetId);
    }

    /**
     * 바이트 배열을 받아서 SUBSCRIBE 객체를 생성하는 생성자.
     *
     * @param payload 바이트 배열로 표현된 SUBSCRIBE 메시지. 이 배열은 패킷 식별자와 주제를 포함해야 합니다.
     * @param offset  바이트 배열에서 메시지가 시작되는 위치. 일반적으로 0으로 설정됩니다.
     * @param length  메시지의 길이. 패킷 식별자와 주제를 포함한 전체 길이여야 합니다.
     * @throws IllegalArgumentException payload가 null이거나, offset이 음수이거나,
     *                                  length가 4보다 작거나, payload의 길이가 offset +
     *                                  length보다 작을 경우 발생합니다.
     */
    public SUBSCRIBE(byte[] payload, Integer offset, Integer length) {
        super(Type.SUBSCRIBE);

        try {
            if ((payload == null) || (offset < 0) || (length < 4) || (payload.length < offset + length)) {
                throw new IllegalArgumentException();
            }

            ByteBuffer buffer = ByteBuffer.wrap(payload, offset, length);
            // 1. Packet Identifier (2바이트)
            packetId = buffer.getShort() & 0xFFFF;
            log.debug("packetId:{}", packetId);

            // 2. Topic 읽기
            byte[] topicBytes = new byte[buffer.getShort() & 0xFFFF];
            buffer.get(topicBytes);
            setQoS(buffer.get());

            this.topic = new String(topicBytes);
            log.debug("topicBytes:{}", new String(topicBytes));
        } catch (BufferUnderflowException e) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * 바이트 배열을 받아서 SUBSCRIBE 객체를 생성하는 생성자.
     * 기본적으로 offset은 0, length는 payload의 길이로 설정됩니다.
     *
     * @param payload 바이트 배열로 표현된 SUBSCRIBE 메시지. 이 배열은 패킷 식별자와 주제를 포함해야 합니다.
     */
    public SUBSCRIBE(byte[] payload) {
        // TODO: 수정하세요.
        this(payload, 0, (payload != null) ? payload.length : 0);
    }

    /**
     * 패킷 식별자를 반환합니다.
     *
     * @return packetId 패킷 식별자. 이 식별자는 SUBSCRIBE 메시지가 응답하는 SUBACK 메시지의 식별자와 동일합니다.
     */
    public int getPacketId() {
        return this.packetId;
    }

    /**
     * 구독할 주제를 반환합니다.
     *
     * @return topic 구독할 주제. 이 주제는 클라이언트가 수신하고자 하는 메시지의 범위를 정의합니다.
     */
    public String getTopic() {
        return this.topic;
    }

    /**
     * 현재 객체를 MQTT SUBSCRIBE 메시지 형식의 바이트 배열로 변환합니다.
     * 이 메서드는 SUBSCRIBE 메시지를 전송하기 위해 사용됩니다.
     *
     * @return MQTT SUBSCRIBE 메시지 형식의 바이트 배열. 이 배열은 패킷 식별자와 주제를 포함합니다.
     */
    @Override
    public byte[] toByteArray() {
        byte[] topicBytes = topic.getBytes();

        ByteBuffer buffer = getByteBuffer(2 + 2 + topicBytes.length + 1);

        // TODO: 패킷을 구성하세요.
        buffer.put(topicBytes);
        
        buffer.putShort((short) packetId);

        return buffer.array();
    }
}
