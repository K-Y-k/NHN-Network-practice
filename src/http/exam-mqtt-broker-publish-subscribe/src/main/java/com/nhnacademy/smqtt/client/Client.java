package com.nhnacademy.smqtt.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import com.nhnacademy.smqtt.message.CONNACK;
import com.nhnacademy.smqtt.message.CONNECT;
import com.nhnacademy.smqtt.message.DISCONNECT;
import com.nhnacademy.smqtt.message.Message;

import lombok.extern.slf4j.Slf4j;

/**
 * MQTT 클라이언트의 추상 클래스입니다. 이 클래스는 MQTT 브로커와의 연결을 관리하고
 * 메시지를 전송 및 수신하는 기능을 제공합니다.
 */
@Slf4j
public abstract class Client implements Runnable {
    private static final ThreadLocal<Integer> packetId = ThreadLocal.withInitial(() -> 0);
    private final String clientId;
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    private boolean cleanSession = true;
    private int qos = 0;

    protected Client() {
        clientId = "";
    }

    /**
     * Client 생성자.
     *
     * @param brokerHost 브로커의 호스트 주소
     * @param brokerPort 브로커의 포트 번호
     * @param clientId   클라이언트 식별자
     * @throws IllegalArgumentException 잘못된 인자가 전달된 경우
     */
    protected Client(String brokerHost, int brokerPort, String clientId) throws IOException {
        // TODO: 인수를 검증하세요. 유효하지 않을 경우, IllegalArgumentException을 발생 시킵니다.
        if (StringUtils.isEmpty(brokerHost) || brokerPort < 0 || StringUtils.isEmpty(clientId)) {
            throw new IllegalArgumentException("invalid : brokerHost or brokerPort or clientId");
        }

        socket = new Socket(brokerHost, brokerPort);
        input = new DataInputStream(socket.getInputStream());
        output = new DataOutputStream(socket.getOutputStream());
        log.info("Connected to MQTT Broker at {}:{}", brokerHost, brokerPort);
        this.clientId = clientId;
    }

    /**
     * 테스트용 Client 생성자.
     *
     * @param socket   클라이언트 소켓
     * @param input    데이터 입력 스트림
     * @param output   데이터 출력 스트림
     * @param clientId 클라이언트 식별자
     * @throws IllegalArgumentException 잘못된 인자가 전달된 경우
     */
    protected Client(Socket socket, DataInputStream input, DataOutputStream output, String clientId) {
        if ((socket == null) || (input == null) || (output == null)) {
            throw new IllegalArgumentException();
        }

        this.socket = socket;
        this.input = input;
        this.output = output;
        log.info("Connected to MQTT Broker at mokito");
        this.clientId = clientId;
    }

    /**
     * 클린 세션 설정.
     *
     * @param cleanSessio 클린 세션 여부
     */
    public void setCleanSession(boolean cleanSessio) {
        this.cleanSession = cleanSessio;
    }

    /**
     * 클린 세션 여부 반환.
     *
     * @return 클린 세션 여부
     */
    public boolean getCleanSession() {
        return cleanSession;
    }

    /**
     * QoS 설정.
     *
     * @param qos QoS 레벨 (0, 1, 2만 허용)
     * @throws IllegalArgumentException 잘못된 QoS 레벨이 전달된 경우
     */
    public void setQoS(int qos) {
        // TODO: qos 값이 유효한지 검증하고, 설정합니다.
        if (qos > 3  || qos < 0) {
            throw new IllegalArgumentException("valid : 0 < qos < 3");
        }

        this.qos = qos;
    }

    /**
     * QoS 레벨 반환.
     *
     * @return QoS 레벨
     */
    public int getQoS() {
        return qos;
    }

    /**
     * MQTT 브로커와 연결 및 메시지 전송을 처리합니다.
     * 이 메서드는 스레드가 실행될 때 호출됩니다.
     */
    public void run() {
        try {
            packetId.set(0);

            // 1. CONNECT 메시지 전송
            sendConnect();

            // 2️. CONNACK 메시지 수신
            receiveConnAck();

            processing();

            // 6. DISCONNECT 메시지 전송 및 종료
            sendDisconnect();
        } catch (IOException e) {
        } finally {
            closeConnection();
            packetId.remove();
        }
    }

    /**
     * MQTT CONNECT 메시지를 전송합니다.
     *
     * @throws IOException 메시지 전송 실패 시 발생
     */
    void sendConnect() throws IOException {
        // TODO: CONNECT 메시지를 생성해 전송합니다.
        send(new CONNECT(clientId));

        log.info("Sent CONNECT: " + clientId);
    }

    /**
     * MQTT CONNACK 메시지를 수신하고 검증합니다.
     *
     * @throws IOException 메시지 수신 실패 시 발생
     */
    void receiveConnAck() throws IOException {
        log.info("Sent CONNECT: " + clientId);
        // TODO: CONNACK 메시지를 수신하여 검증합니다. 메시지를 유효하지 않을 경우, 연결을 끊습니다.
        
        throw new IllegalArgumentException();
    }

    /**
     * MQTT DISCONNECT 메시지를 전송합니다.
     *
     * @throws IOException 메시지 전송 실패 시 발생
     */
    protected void sendDisconnect() throws IOException {
        send(new DISCONNECT());
        log.info("Sent DISCONNECT.");
    }

    /**
     * 메시지 처리 로직을 구현하기 위한 메서드입니다.
     * 이 메서드는 서브클래스에서 오버라이드하여 사용합니다.
     *
     * @throws IOException 메시지 처리 중 오류 발생 시
     */
    protected void processing() throws IOException {
    }

    /**
     * 주어진 메시지를 전송합니다.
     *
     * @param message 전송할 메시지 객체
     * @throws IOException 메시지 전송 실패 시 발생
     */
    protected void send(Message message) throws IOException {
        message.setQoS(qos);

        // TODO: 소켓을 통해 메시지를 전송합니다.
        output.write(message.toByteArray());
        output.flush();

        log.debug("output.write:{}", message.toString().getBytes());
    }

    /**
     * 연결을 종료합니다.
     */
    void closeConnection() {
        // TODO: 연결을 끊을때, 자원을 정리합니다.
        log.info("Disconnected from MQTT Broker.");
        try {
            input.close();
            output.close();
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException("input/output/socket close error");
        }
    }

    /**
     * 메시지를 수신합니다.
     *
     * @return 수신된 메시지 객체
     * @throws IOException 메시지 수신 실패 시 발생
     */
    protected Message receive() throws IOException {
        byte[] buffer = input.readAllBytes();

        log.debug("byte[] buffer:{}", Arrays.toString(buffer));

        // TODO: 데이터를 받아 기본 메시지 형식이 맞는지 확인합니다. fixed header 정보만 이용합니다.
        // if (buffer[0] == null || buffer[1] == null) {
            
        // }
        
        return Message.parsing(buffer);
    }

    /**
     * 패킷 ID를 반환합니다.
     *
     * @return 패킷 ID
     */
    public static int getPacketId() {
        // TODO: 패킷 아이디 호출 할때마다 증가합니다.
        int currentPacketId = packetId.get();
        int increasePacketId = currentPacketId + 1;
        packetId.set(increasePacketId);
        return currentPacketId;
    }
}
