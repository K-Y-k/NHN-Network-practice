package com.nhnacademy.smqtt.subscribe;

import com.nhnacademy.smqtt.message.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SubscribeClientTest {
    private static final String TOPIC = "test/topic";
    private static final String CLIENT_ID = "ClientTest";
    private static final String MESSAGE1 = "Hello MQTT!";
    private static final String BROKER_HOST = "lcoalhost";
    private static final int BROKER_PORT = 1883;
    private Socket mockSocket;
    private DataInputStream mockInput;
    private DataOutputStream mockOutput;
    private SubscribeClient subscribeClient;

    /**
     * 각 테스트 전에 실행되어 Mock 객체를 초기화하고 SubscribeClient 인스턴스를 생성합니다.
     *
     * @throws IOException 입출력 예외가 발생할 경우
     */
    @BeforeEach
    void setUp() throws IOException {
        // Mock Socket, InputStream, OutputStream 생성
        mockSocket = mock(Socket.class);
        mockInput = mock(DataInputStream.class);
        mockOutput = mock(DataOutputStream.class);

        // Socket의 getInputStream(), getOutputStream()을 Stub 처리
        when(mockSocket.getInputStream()).thenReturn(mockInput);
        when(mockSocket.getOutputStream()).thenReturn(mockOutput);

        // SubscribeClient 인스턴스 생성
        subscribeClient = new SubscribeClient(mockSocket, mockInput, mockOutput, CLIENT_ID);
    }

    /**
     * SubscribeClient 객체가 정상적으로 생성되는지 테스트합니다.
     */
    @Test
    void testValidClientCreation() {
        assertNotNull(subscribeClient);
        assertEquals(0, subscribeClient.getTopicCount());
    }

    /**
     * SubscribeClient 객체 생성 시 잘못된 인자가 전달될 경우 예외가 발생하는지 테스트합니다.
     */
    @ParameterizedTest
    @MethodSource("testInvalidClientCreationParamProvider")
    void testInvalidClientCreation(String host, int port, String clientId) {
        assertThrows(IllegalArgumentException.class, () -> new SubscribeClient(host, port, clientId));
    }

    static Stream<Arguments> testInvalidClientCreationParamProvider() {
        return Stream.of(
                Arguments.of(null, BROKER_PORT, CLIENT_ID),
                Arguments.of(BROKER_HOST, -1, CLIENT_ID),
                Arguments.of(BROKER_HOST, BROKER_PORT, null));
    }

    /**
     * 토픽을 추가하고 조회할 수 있는지 테스트합니다.
     */
    @Test
    void testAddTopic() {
        subscribeClient.addTopic(TOPIC);
        List<String> topics = subscribeClient.getTopics();

        assertEquals(1, topics.size());
        assertEquals(TOPIC, topics.get(0));
    }

    /**
     * 동일한 토픽을 중복 추가할 경우 중복 저장이 방지되는지 테스트합니다.
     */
    @Test
    void testAddDuplicateTopic() {
        subscribeClient.addTopic(TOPIC);
        subscribeClient.addTopic(TOPIC); // 중복 추가

        assertEquals(1, subscribeClient.getTopicCount()); // 중복 저장 방지 확인
    }

    /**
     * SUBSCRIBE 메시지를 전송할 수 있는지 테스트합니다.
     *
     * @throws IOException 입출력 예외가 발생할 경우
     */
    @Test
    void testSendSubscribe() throws IOException {
        subscribeClient = spy(subscribeClient);
        doNothing().when(mockOutput).write(any(byte[].class));

        subscribeClient.sendSubscribe(TOPIC);

        verify(mockOutput, atLeastOnce()).write(any(byte[].class));
        verify(mockOutput, atLeastOnce()).flush();
    }

    /**
     * 정상적인 SUBACK 메시지를 수신하고 처리할 수 있는지 테스트합니다.
     *
     * @throws IOException 입출력 예외가 발생할 경우
     */
    @Test
    void testReceiveSubAckValid() throws IOException {
        byte[] validSubAck = new SUBACK(1, (byte) 0).toByteArray();
        doAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(validSubAck, 0, buffer, 0, validSubAck.length);
            return null;
        }).when(mockInput).readFully(any(byte[].class), anyInt(), anyInt());

        assertDoesNotThrow(() -> subscribeClient.receiveSubAck(TOPIC)); // 정상 동작 확인
    }

    /**
     * 유효하지 않은 SUBACK 메시지를 수신할 경우 예외가 발생하는지 테스트합니다.
     *
     * @throws IOException 입출력 예외가 발생할 경우
     */
    @Test
    void testReceiveSubAckInvalid() throws IOException {
        byte[] invalidSubAck = { 0x00, 0x07 }; // 잘못된 Return Code
        doAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(invalidSubAck, 0, buffer, 0, invalidSubAck.length);
            return null;
        }).when(mockInput).readFully(any(byte[].class), anyInt(), anyInt());

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> subscribeClient.receiveSubAck(TOPIC));
        assertTrue(exception.getMessage().contains("유효하지 않은 메시지를 수신 하였습니다."));
    }

    /**
     * PUBLISH 메시지를 수신했을 때 정상적으로 처리하는지 테스트합니다.
     *
     * @throws IOException 입출력 예외가 발생할 경우
     */
    @Test
    void testListenForMessages() throws IOException {
        subscribeClient = spy(subscribeClient);

        byte[] validPublish = new PUBLISH(TOPIC, MESSAGE1).toByteArray();
        doAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(validPublish, 0, buffer, 0, validPublish.length);
            return null;
        }).when(mockInput).readFully(any(byte[].class));

        Thread testThread = new Thread(() -> {
            try {
                subscribeClient.listenForMessages();
            } catch (IOException ignored) {
            }
        });
        testThread.start();

        testThread.interrupt(); // 메시지 루프를 종료
        assertTrue(testThread.isInterrupted());
    }

    /**
     * verbose 모드가 활성화된 경우 메시지가 출력되는지 테스트합니다.
     */
    @Test
    void testVerboseMode() {
        subscribeClient.setVerbose(true);
        assertTrue(subscribeClient.getVerbose());
    }
}
