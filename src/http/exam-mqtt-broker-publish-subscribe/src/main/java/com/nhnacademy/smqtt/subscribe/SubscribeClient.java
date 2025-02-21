package com.nhnacademy.smqtt.subscribe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.nhnacademy.smqtt.broker.Broker;
import com.nhnacademy.smqtt.client.Client;
import com.nhnacademy.smqtt.message.Message;
import com.nhnacademy.smqtt.message.PUBLISH;
import com.nhnacademy.smqtt.message.SUBACK;
import com.nhnacademy.smqtt.message.SUBSCRIBE;

import lombok.extern.slf4j.Slf4j;

/**
 * SubscribeClient 클래스는 MQTT 프로토콜을 사용하여 특정 토픽을 구독하는 클라이언트입니다.
 * 이 클래스는 Client 클래스를 상속받아 MQTT 브로커와의 연결을 관리하고 메시지를 수신하는 기능을 제공합니다.
 */
@Slf4j
public class SubscribeClient extends Client {

    private final List<String> topics = new LinkedList<>();
    private boolean verbose = false;

    /**
     * SubscribeClient 생성자.
     *
     * @param brokerHost 브로커의 호스트 주소
     * @param brokerPort 브로커의 포트 번호
     * @param clientId   클라이언트 식별자
     * @throws IOException 연결 중 오류가 발생한 경우
     */
    public SubscribeClient(String brokerHost, int brokerPort, String clientId) throws IOException {
        super(brokerHost, brokerPort, clientId);
    }

    public SubscribeClient(Socket socket, DataInputStream input, DataOutputStream output, String clientId)
            throws IOException {
        super(socket, input, output, clientId);
    }

    /**
     * verbose 모드 설정.
     *
     * @param verbose verbose 모드 여부
     */
    public void setVerbose(boolean verbose) {
        // TODO: verbose 옵션을 설정합니다.
        this.verbose = verbose;
    }

    /**
     * verbose 모드 여부 반환.
     *
     * @return verbose 모드 여부
     */
    public boolean getVerbose() {
        return verbose;
    }

    /**
     * 구독할 토픽을 추가합니다.
     *
     * @param topic 구독할 토픽
     * @throws IllegalArgumentException 토픽이 null인 경우
     */
    public void addTopic(String topic) {
        // TODO: topic을 관리하세요.
        if (Objects.isNull(topic)) {
            throw new IllegalArgumentException("invalid : topic is null");
        }

        if (!topics.contains(topic)) {
            topics.add(topic);
        }
    }

    /**
     * 구독 중인 모든 토픽을 반환합니다.
     *
     * @return 구독 중인 토픽 목록
     */
    public List<String> getTopics() {
        return topics;
    }

    /**
     * 인덱스에 해당하는 토픽을 반환합니다.
     *
     * @param index 토픽의 인덱스
     * @return 인덱스에 해당하는 토픽
     */
    public String getTopic(int index) {
        return topics.get(index);
    }

    /**
     * 구독 중인 토픽의 수를 반환합니다.
     *
     * @return 구독 중인 토픽의 수
     */
    public int getTopicCount() {
        return topics.size();
    }

    /**
     * MQTT 브로커와 연결을 수행하고 메시지를 처리합니다.
     * 클린 세션이 설정된 경우, 모든 구독 토픽에 대해 SUBSCRIBE 메시지를 전송하고
     * SUBACK 메시지를 수신합니다. 이후 PUBLISH 메시지를 지속적으로 수신합니다.
     *
     * @throws IOException 메시지 전송 또는 수신 중 오류가 발생한 경우
     */
    @Override
    protected void processing() throws IOException {
        // TODO: clean session인 경우, 추가되어 있는 토픽을 브로커에 등록합니다.

        // TODO: 해당 토픽 메시지를 수신합니다.
    }

    /**
     * MQTT SUBSCRIBE 메시지를 전송합니다.
     *
     * @param topic 구독할 토픽
     * @throws IOException 메시지 전송 중 오류가 발생한 경우
     */
    protected void sendSubscribe(String topic) throws IOException {
        // TODO: 토픽을 브로커에 등록합니다.
        log.info("Sent SUBSCRIBE message for topic: {}", topic);
        
        
    }

    /**
     * MQTT SUBACK 메시지를 수신하고 검증합니다.
     * 수신한 메시지가 SUBACK인 경우, 구독이 성공적으로 이루어졌는지 확인합니다.
     *
     * @param topic 구독한 토픽
     * @throws IOException 유효하지 않은 메시지를 수신하거나 구독 실패 시
     */
    protected void receiveSubAck(String topic) throws IOException {
        Message message = receive();
        if (message instanceof SUBACK subAck) {
            if (subAck.getReturnCode() == 0x00) {
                log.info("Subscription to topic '{}' confirmed!", topic);
            } else {
                throw new IOException(
                        String.format("Subscription to topic %s failed with code: %d", topic, subAck.getReturnCode()));
            }
        } else {
            throw new IOException("유효하지 않은 메시지를 수신 하였습니다.");
        }
    }

    /**
     * MQTT PUBLISH 메시지를 지속적으로 수신합니다.
     * 수신한 메시지가 PUBLISH인 경우, 메시지를 출력합니다.
     * verbose 모드가 활성화된 경우, 토픽 정보도 함께 출력합니다.
     *
     * @throws IOException 메시지 수신 중 오류가 발생한 경우
     */
    protected void listenForMessages() throws IOException {
        // TODO: PUBLISH 메시지를 반복해서 수신하여, 터미널로 출력합니다.
    }
}
