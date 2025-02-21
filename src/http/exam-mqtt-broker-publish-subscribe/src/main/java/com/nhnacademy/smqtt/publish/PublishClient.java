package com.nhnacademy.smqtt.publish;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.nhnacademy.smqtt.client.Client;
import com.nhnacademy.smqtt.message.Message;
import com.nhnacademy.smqtt.message.PUBACK;
import com.nhnacademy.smqtt.message.PUBLISH;

import lombok.extern.slf4j.Slf4j;

/**
 * PublishClient 클래스는 MQTT 프로토콜을 사용하여 메시지를 특정 토픽으로 발행하는 클라이언트입니다.
 * 이 클래스는 Client 클래스를 상속받아 MQTT 브로커와의 연결을 관리하고 메시지를 전송하는 기능을 제공합니다.
 */
@Slf4j
public class PublishClient extends Client {
    // TODO: 생성시 주어지는 topic을 관리합니다.
    private String topic;
    InputStream messageIn;
    Map<Integer, PUBLISH> publishMap = new ConcurrentHashMap<>();

    /**
     * PublishClient 생성자.
     *
     * @param brokerHost 브로커의 호스트 주소
     * @param brokerPort 브로커의 포트 번호
     * @param clientId   클라이언트 식별자
     * @param topic      메시지를 발행할 토픽
     * @param messageIn  메시지를 읽어올 InputStream
     * @throws IllegalArgumentException 토픽이 null인 경우
     */
    public PublishClient(String brokerHost, int brokerPort, String clientId, String topic, InputStream messageIn)
            throws IOException {
        // TODO: 생성자를 적절히 사용하세요.
        super(brokerHost, brokerPort, clientId);

        if (topic == null) {
            throw new IllegalArgumentException();
        }

        this.topic = topic;
        this.messageIn = messageIn;
    }

    public PublishClient(Socket socket, DataInputStream input, DataOutputStream output, String clientId, String topic,
            InputStream messageIn) {
        // TODO: 생성자를 적절히 사용하세요.
        super(socket, input, output, clientId);

        if (topic == null) {
            throw new IllegalArgumentException();
        }

        this.topic = topic;
        this.messageIn = messageIn;
    }

    public String getTopic() {
        return topic;
    }

    /**
     * 메시지를 읽고 PUBLISH 메시지를 전송하며, 필요 시 PUBACK 메시지를 수신합니다.
     *
     * @throws IOException 메시지 전송 또는 수신 중 오류가 발생한 경우
     */
    @Override
    public void processing() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(messageIn));
        String line;

        while ((line = reader.readLine()) != null) {
            // 1️. PUBLISH 메시지 전송
            sendPublish(topic, line);

            if (getQoS() == 1) {
                // 2️. PUBACK 메시지 수신
                receivePubAck(topic);
            }
        }
    }

    /**
     * MQTT PUBLISH 메시지를 전송합니다.
     *
     * @param topic   메시지를 발행할 토픽
     * @param message 발행할 메시지
     * @throws IOException 메시지 전송 중 오류가 발생한 경우
     */
    void sendPublish(String topic, String message) throws IOException {
        PUBLISH publish = new PUBLISH(topic, message);
        send(publish);

        publishMap.put(publish.getPacketId(), publish);
        log.info("Published message to topic '" + topic + "': " + message);
    }

    /**
     * MQTT PUBACK 메시지를 수신하고 검증합니다.
     *
     * @param topic 메시지를 발행한 토픽
     * @throws IOException 유효하지 않은 메시지를 수신한 경우
     */
    void receivePubAck(String topic) throws IOException {
        Message message = receive();
        if ((message instanceof PUBACK pubAck) && (publishMap.containsKey(pubAck.getPacketId()))) {
            publishMap.remove(pubAck.getPacketId());
            log.info("Published topic '" + topic + "' confirmed!");
        } else {
            throw new IOException("유효하지 않은 메시지를 수신 하였습니다.");
        }
    }

}
