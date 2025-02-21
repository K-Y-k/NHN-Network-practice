package com.nhnacademy.smqtt.message;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import com.nhnacademy.smqtt.client.Client;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PUBLISH extends Message {
    private final String topic;
    private final String message;
    private final int packetId;

    /**
     * PUBLISH 메시지를 직접 생성하는 생성자.
     *
     * @param topic   메시지가 게시될 주제. 주제는 null이거나 비어 있을 수 없습니다.
     * @param message 게시할 메시지 내용.
     * @throws IllegalArgumentException 주제가 null이거나 비어 있는 경우 발생합니다.
     */
    public PUBLISH(String topic, String message) {
        super(Type.PUBLISH);

        if (topic == null || topic.isEmpty()) {
            throw new IllegalArgumentException("Topic은 비어 있을 수 없습니다.");
        }
        this.topic = topic;
        this.message = message;
        packetId = Client.getPacketId();
    }

    /**
     * 바이트 배열을 받아서 PUBLISHMessage 객체를 생성하는 생성자.
     *
     * @param data   바이트 배열로 표현된 PUBLISH 메시지. 이 배열은 주제, 패킷 ID 및 메시지 내용을 포함해야 합니다.
     * @param offset 바이트 배열에서 메시지가 시작되는 위치. 일반적으로 0으로 설정됩니다.
     * @param length 메시지의 길이. 주제와 메시지 내용을 포함한 전체 길이여야 합니다.
     * @throws IllegalArgumentException payload가 null이거나, offset이 음수이거나,
     *                                  length가 2보다 작거나, payload의 길이가 offset +
     *                                  length보다 작을 경우 발생합니다.
     */
    public PUBLISH(byte[] data, Integer offset, Integer length) {
        super(Type.PUBLISH);

        // TODO: 인수를 검증하고, 패킷을 분석하여 각 필드를 채워 줍니다.
        if ((data == null) || (offset < 0) || (length < 2) || (data.length < offset + length)) {
            throw new IllegalArgumentException("PUBLISH 메시지는 4바이트여야 합니다.");
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(data, offset, length);

        try {
            // 1. Topic
            byte[] topicBytes = new byte[buffer.getShort() & 0xFFFF];
            buffer.get(topicBytes);
            
            this.topic = new String(topicBytes);
            log.debug("topic:{}", this.topic);

            // 2. 패킷 ID
            this.packetId = buffer.getShort();
            log.debug("packetId:{}", this.packetId);

            // 3. 메시지
            int position = buffer.position();

            byte[] messageBytes = new byte[data.length-position];

            for (int i = 0; i < data.length-position; i++) {
                messageBytes[i] = buffer.get();
            }

            this.message = new String(messageBytes);
            log.debug("message:{}", this.message);
        } catch (BufferUnderflowException ignore) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * 바이트 배열을 받아서 PUBLISH 객체를 생성하는 생성자.
     * 기본적으로 offset은 0, length는 payload의 길이로 설정됩니다.
     *
     * @param payload 바이트 배열로 표현된 PUBLISH 메시지.
     */
    public PUBLISH(byte[] payload) {
        this(payload, 0, (payload != null) ? payload.length : 0);
    }

    /**
     * 주제를 반환합니다.
     *
     * @return topic 메시지가 게시될 주제.
     */
    public String getTopic() {
        return topic;
    }

    /**
     * 메시지 내용을 반환합니다.
     *
     * @return message 게시할 메시지 내용.
     */
    public String getMessage() {
        return message;
    }

    /**
     * 패킷 식별자를 반환합니다.
     *
     * @return packetId 패킷 식별자. 이 식별자는 메시지의 고유 식별자입니다.
     */
    public int getPacketId() {
        return packetId;
    }

    /**
     * 현재 객체를 MQTT PUBLISH 메시지 형식의 바이트 배열로 변환합니다.
     * 이 메서드는 PUBLISH 메시지를 전송하기 위해 사용됩니다.
     *
     * @return MQTT PUBLISH 메시지 형식의 바이트 배열. 이 배열은 주제와 메시지 내용을 포함합니다.
     */
    @Override
    public byte[] toByteArray() {
        byte[] topicBytes = topic.getBytes(StandardCharsets.UTF_8);
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);

        ByteBuffer buffer = getByteBuffer(2 + topicBytes.length + 2 + messageBytes.length);
        buffer.putShort((short) topicBytes.length);
        buffer.put(topicBytes);
        buffer.putShort((short) packetId);
        buffer.put(messageBytes);

        return buffer.array();
    }
}
