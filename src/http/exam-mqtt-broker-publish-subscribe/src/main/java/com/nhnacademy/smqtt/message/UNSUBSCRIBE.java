package com.nhnacademy.smqtt.message;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import com.nhnacademy.smqtt.client.Client;

/**
 * UNSUBSCRIBE 클래스는 MQTT 프로토콜의 UNSUBSCRIBE 메시지를 나타냅니다.
 * 이 메시지는 클라이언트가 브로커에게 특정 주제에 대한 구독을 취소하기 위해 전송됩니다.
 * 주제는 클라이언트가 더 이상 수신하고 싶지 않은 메시지의 범위를 정의합니다.
 */
public class UNSUBSCRIBE extends Message {
    private final int packetId;
    private final List<String> topics = new LinkedList<>();

    /**
     * 기본 생성자. 패킷 식별자를 0으로 초기화합니다.
     */
    public UNSUBSCRIBE() {
        super(Type.UNSUBSCRIBE);
        this.packetId = 0;
    }

    /**
     * 주어진 주제 목록을 사용하여 UNSUBSCRIBE 메시지를 생성하는 생성자.
     *
     * @param topics 구독 취소할 주제 목록. 각 주제는 클라이언트가 더 이상 수신하고 싶지 않은 메시지의 범위를 정의합니다.
     */
    public UNSUBSCRIBE(String[] topics) {
        super(Type.UNSUBSCRIBE);
        this.packetId = 0;
        for (String topic : topics) {
            this.topics.add(topic);
        }
    }

    /**
     * 바이트 배열을 받아서 UNSUBSCRIBE 객체를 생성하는 생성자.
     *
     * @param payload 바이트 배열로 표현된 UNSUBSCRIBE 메시지. 이 배열은 패킷 식별자와 주제를 포함해야 합니다.
     * @param offset  바이트 배열에서 메시지가 시작되는 위치. 일반적으로 0으로 설정됩니다.
     * @param length  메시지의 길이. 패킷 식별자와 주제를 포함한 전체 길이여야 합니다.
     * @throws IllegalArgumentException payload가 null이거나, offset이 음수이거나,
     *                                  length가 4보다 작거나, payload의 길이가 offset +
     *                                  length보다 작을 경우 발생합니다.
     */
    public UNSUBSCRIBE(byte[] payload, Integer offset, Integer length) {
        super(Type.UNSUBSCRIBE);
        try {
            if ((payload == null) || (offset < 0) || (length < 4) || (payload.length < offset + length)) {
                throw new IllegalArgumentException();
            }

            ByteBuffer buffer = ByteBuffer.wrap(payload, offset, length);
            // 1. Packet Identifier 읽기
            this.packetId = buffer.getShort() & 0xFFFF;

            // 2. Topic 읽기
            if (buffer.hasRemaining()) {
                int topicLength = buffer.getShort();
                byte[] topicBytes = new byte[topicLength];
                buffer.get(topicBytes);
                addTopic(new String(topicBytes, StandardCharsets.UTF_8));
            }
        } catch (BufferUnderflowException e) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * 패킷 식별자를 반환합니다.
     *
     * @return packetId 패킷 식별자. 이 식별자는 UNSUBSCRIBE 메시지가 응답하는 SUBSCRIBE 메시지의 식별자와
     *         동일합니다.
     */
    public int getPacketId() {
        return packetId;
    }

    /**
     * 구독 취소할 주제 목록을 반환합니다.
     *
     * @return topics 구독 취소할 주제 목록.
     */
    public List<String> getTopics() {
        return topics;
    }

    /**
     * 주어진 인덱스에 해당하는 주제를 반환합니다.
     *
     * @param index 주제를 가져올 인덱스.
     * @return 주어진 인덱스에 해당하는 주제.
     */
    public String getTopic(int index) {
        return topics.get(index);
    }

    /**
     * 주제를 추가합니다.
     *
     * @param topic 추가할 주제. 이 주제는 클라이언트가 더 이상 수신하고 싶지 않은 메시지의 범위를 정의합니다.
     */
    public void addTopic(String topic) {
        topics.add(topic);
    }

    /**
     * 현재 객체를 MQTT UNSUBSCRIBE 메시지 형식의 바이트 배열로 변환합니다.
     * 이 메서드는 UNSUBSCRIBE 메시지를 전송하기 위해 사용됩니다.
     *
     * @return MQTT UNSUBSCRIBE 메시지 형식의 바이트 배열. 이 배열은 패킷 식별자와 주제를 포함합니다.
     */
    @Override
    public byte[] toByteArray() {
        int bufferSize = 2;

        for (String topic : topics) {
            bufferSize += 2 + topic.getBytes(StandardCharsets.UTF_8).length;
        }

        ByteBuffer buffer = getByteBuffer(bufferSize);
        buffer.putShort((short) Client.getPacketId());
        for (String topic : topics) {
            byte[] topicBytes = topic.getBytes(StandardCharsets.UTF_8);
            buffer.putShort((short) topicBytes.length);
            buffer.put(topicBytes);
        }

        return buffer.array();
    }
}
