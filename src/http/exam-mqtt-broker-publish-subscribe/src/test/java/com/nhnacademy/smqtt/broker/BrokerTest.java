package com.nhnacademy.smqtt.broker;

import com.nhnacademy.smqtt.message.*;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.*;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class BrokerTest {
    private static final String TOPIC = "test/topic";
    private static final String CLIENT_ID = "ClientTest";
    private static final String MESSAGE1 = "Hello MQTT!";
    private static final String BROKER_HOST = "localhost";
    private static final int BROKER_PORT = 1883;
    private static final int BROKER_PORT2 = 18883;
    private static Broker broker;
    private static ExecutorService executorService;

    /**
     * 모든 테스트 전에 실행되어 Broker 인스턴스를 생성하고 실행합니다.
     */
    @BeforeAll
    static void setUp() {
        broker = new Broker(BROKER_PORT);
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(broker);
    }

    /**
     * 모든 테스트 후에 실행되어 Broker를 중단하고 ExecutorService를 종료합니다.
     */
    @AfterAll
    static void tearDown() {
        broker.interrupt();
        executorService.shutdownNow();
    }

    /**
     * Broker가 정상적으로 실행되는지 테스트
     *
     * @throws IOException 입출력 예외가 발생할 경우
     */
    @Test
    void testBrokerStartsSuccessfully() throws IOException {
        assertDoesNotThrow(() -> {
            Broker broker = new Broker(BROKER_PORT2);

            broker.start();

            Thread.sleep(2000);

            assertTrue(broker.isAlive());
            broker.interrupt();
        });
    }

    /**
     * 클라이언트가 CONNECT 메시지를 보내고 CONNACK 응답을 받는지 테스트
     *
     * @throws IOException 입출력 예외가 발생할 경우
     */
    @Test
    void testClientConnectsAndReceivesConnAck() throws IOException {
        try (Socket socket = new Socket(BROKER_HOST, BROKER_PORT);
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                DataInputStream input = new DataInputStream(socket.getInputStream())) {

            log.info("testClientConnectsAndReceivesConnAck start: {}", socket.getLocalPort());
            // CONNECT 메시지 전송
            send(output, new CONNECT(CLIENT_ID));

            // CONNACK 메시지 수신
            Message message = receive(input);
            assertTrue(message instanceof CONNACK);

            // DISCONNECT 메시지 전송
            send(output, new DISCONNECT());
            assertEquals(0, ((CONNACK) message).getReturnCode());

            log.info("testClientConnectsAndReceivesConnAck end: {}", socket.getLocalPort());
        }
    }

    /**
     * 클라이언트가 SUBSCRIBE 요청을 보내고 SUBACK을 받는지 테스트
     *
     * @throws IOException 입출력 예외가 발생할 경우
     */
    @Test
    void testClientSubscribesAndReceivesSubAck() throws IOException {
        try (Socket socket = new Socket(BROKER_HOST, BROKER_PORT);
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                DataInputStream input = new DataInputStream(socket.getInputStream())) {

            log.info("testClientSubscribesAndReceivesSubAck start: {}", socket.getLocalPort());

            // CONNECT 메시지 전송
            send(output, new CONNECT(CLIENT_ID));

            // CONNACK 메시지 수신
            Message message = receive(input);
            assertTrue(message instanceof CONNACK);

            // SUBSCRIBE 메시지 전송
            send(output, new SUBSCRIBE(TOPIC));

            // SUBACK 메시지 수신
            message = receive(input);
            assertTrue(message instanceof SUBACK);

            // DISCONNECT 메시지 전송
            send(output, new DISCONNECT());

            log.info("testClientSubscribesAndReceivesSubAck end: {}", socket.getLocalPort());
        }
    }

    /**
     * 클라이언트가 PUBLISH 메시지를 보내고 브로커가 이를 전파하는지 테스트
     *
     * @throws IOException 입출력 예외가 발생할 경우
     */
    @Test
    void testClientPublishesMessage() throws IOException {
        try (Socket socket1 = new Socket(BROKER_HOST, BROKER_PORT);
                DataOutputStream output1 = new DataOutputStream(socket1.getOutputStream());
                DataInputStream input1 = new DataInputStream(socket1.getInputStream());

                Socket socket2 = new Socket(BROKER_HOST, BROKER_PORT);
                DataOutputStream output2 = new DataOutputStream(socket2.getOutputStream());
                DataInputStream input2 = new DataInputStream(socket2.getInputStream())) {

            log.info("testClientPublishesMessage start: {}", socket1.getLocalPort());

            // 두 번째 클라이언트가 SUBSCRIBE
            send(output2, new CONNECT("Subscriber"));

            Message message = receive(input2);
            assertTrue(message instanceof CONNACK);

            SUBSCRIBE subscribe = new SUBSCRIBE(TOPIC);
            subscribe.setQoS(1);
            send(output2, subscribe);

            message = receive(input2);
            assertTrue(message instanceof SUBACK);

            // 첫 번째 클라이언트가 PUBLISH
            send(output1, new CONNECT("Publisher"));

            message = receive(input1);
            assertTrue(message instanceof CONNACK);

            PUBLISH publish = new PUBLISH(TOPIC, MESSAGE1);
            send(output1, publish);

            // 두 번째 클라이언트가 PUBLISH 메시지를 받는지 확인
            message = receive(input2);
            assertTrue(message instanceof PUBLISH);

            assertEquals(TOPIC, ((PUBLISH) message).getTopic());
            assertEquals(MESSAGE1, ((PUBLISH) message).getMessage());

            // DISCONNECT 메시지 전송
            send(output1, new DISCONNECT());
            send(output2, new DISCONNECT());

            log.info("testClientPublishesMessage end: {}", socket1.getLocalPort());
        }
    }

    /**
     * 클라이언트가 UNSUBSCRIBE 요청을 보내고 브로커가 이를 처리하는지 테스트
     *
     * @throws IOException 입출력 예외가 발생할 경우
     */
    @Test
    void testClientUnsubscribes() throws IOException {
        try (Socket socket = new Socket(BROKER_HOST, BROKER_PORT);
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                DataInputStream input = new DataInputStream(socket.getInputStream())) {
            final String topic = TOPIC;
            final String payload = MESSAGE1;

            log.info("testClientUnsubscribes start: {}", socket.getLocalPort());

            // CONNECT 메시지 전송
            send(output, new CONNECT(CLIENT_ID));
            Message message = receive(input);
            assertTrue(message instanceof CONNACK);

            // SUBSCRIBE 메시지 전송
            send(output, new SUBSCRIBE(topic));
            message = receive(input);
            assertTrue(message instanceof SUBACK);

            // UNSUBSCRIBE 메시지 전송
            send(output, new UNSUBSCRIBE(new String[] { topic }));

            // 다시 PUBLISH 후 메시지를 받지 않는지 확인
            send(output, new PUBLISH(topic, payload));

            // 2초 기다렸다가 데이터가 수신되지 않았는지 확인
            socket.setSoTimeout(2000);
            assertThrows(IOException.class, () -> {
                receive(input);
            });

            // DISCONNECT 메시지 전송
            send(output, new DISCONNECT());

            log.info("testClientUnsubscribes end: {}", socket.getLocalPort());
        }
    }

    /**
     * 클라이언트가 PINGREQ 메시지를 보내고 PINGRESP을 받는지 테스트
     *
     * @throws IOException 입출력 예외가 발생할 경우
     */
    @Test
    void testClientSendsPingReq() throws IOException {
        try (Socket socket = new Socket(BROKER_HOST, BROKER_PORT);
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                DataInputStream input = new DataInputStream(socket.getInputStream())) {

            log.info("testClientSendsPingReq start: {}", socket.getLocalPort());

            // CONNECT 메시지 전송
            send(output, new CONNECT(CLIENT_ID));

            Message message = receive(input);
            assertTrue(message instanceof CONNACK);

            // PINGREQ 메시지 전송
            send(output, new PINGREQ());

            // PINGRESP 메시지 수신
            message = receive(input);
            assertTrue(message instanceof PINGRESP);

            // DISCONNECT 메시지 전송
            send(output, new DISCONNECT());
            log.info("testClientSendsPingReq end: {}", socket.getLocalPort());
        }
    }

    /**
     * 주어진 메시지를 DataOutputStream을 통해 전송합니다.
     *
     * @param output  DataOutputStream 객체
     * @param message 전송할 메시지 객체
     * @throws IOException 입출력 예외가 발생할 경우
     */
    static void send(DataOutputStream output, Message message) throws IOException {
        log.info("Send: {}", message.toString());
        output.write(message.toByteArray());
        output.flush();
    }

    /**
     * DataInputStream을 통해 메시지를 수신합니다.
     *
     * @param input DataInputStream 객체
     * @return 수신한 메시지 객체
     * @throws IOException 입출력 예외가 발생할 경우
     */
    static Message receive(DataInputStream input) throws IOException {
        byte[] header = new byte[5];

        input.readFully(header, 0, 2);

        int lengthFieldCount = 1;
        int length = header[1] & 0x7F;
        while ((header[lengthFieldCount] & 0x80) == 0x80) {
            lengthFieldCount++;
            input.readFully(header, lengthFieldCount, 1);
            length += (int) ((header[lengthFieldCount] & 0x7F) * Math.pow(128, lengthFieldCount - 1.0));
        }

        byte[] buffer = new byte[1 + lengthFieldCount + length];
        System.arraycopy(header, 0, buffer, 0, 1 + lengthFieldCount);
        input.readFully(buffer, 1 + lengthFieldCount, length);

        return Message.parsing(buffer);
    }

}
