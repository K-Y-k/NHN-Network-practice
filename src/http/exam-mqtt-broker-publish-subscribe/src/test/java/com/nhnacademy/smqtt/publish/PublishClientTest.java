package com.nhnacademy.smqtt.publish;

import com.nhnacademy.smqtt.message.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PublishClientTest {
    private static final String TOPIC = "test/topic";
    private static final String CLIENT_ID = "ClientTest";
    private static final String MESSAGE1 = "Hello MQTT!";
    private static final String MESSAGE2 = "Test Message";
    private static final String BROKER_HOST = "lcoalhost";
    private static final int BROKER_PORT = 1883;

    private Socket mockSocket;
    private DataInputStream mockInput;
    private DataOutputStream mockOutput;
    private PublishClient publishClient;
    private InputStream mockMessageStream;

    /**
     * 각 테스트 전에 실행되어 Mock 객체를 초기화하고 PublishClient 인스턴스를 생성합니다.
     *
     * @throws IOException 입출력 예외가 발생할 경우
     */
    @BeforeEach
    void setUp() throws IOException {
        // Mock Socket, InputStream, OutputStream 생성
        mockSocket = mock(Socket.class);
        mockInput = mock(DataInputStream.class);
        mockOutput = mock(DataOutputStream.class);

        // 메시지 입력 스트림을 모킹 (가짜 메시지 입력)
        mockMessageStream = new ByteArrayInputStream((MESSAGE1 + "\n" + MESSAGE2 + "\n").getBytes());

        // Socket의 getInputStream(), getOutputStream()을 Stub 처리
        when(mockSocket.getInputStream()).thenReturn(mockInput);
        when(mockSocket.getOutputStream()).thenReturn(mockOutput);

        // PublishClient 인스턴스 생성
        publishClient = new PublishClient(mockSocket, mockInput, mockOutput,
                CLIENT_ID, TOPIC, mockMessageStream) {
        };
    }

    /**
     * PublishClient 객체가 정상적으로 생성되는지 테스트합니다.
     */
    @Test
    void testValidClientCreation() {
        assertNotNull(publishClient);
        assertEquals(TOPIC, publishClient.getTopic());
    }

    /**
     * 잘못된 호스트로 PublishClient 객체를 생성할 때 예외가 발생하는지 테스트합니다.
     */
    @Test
    void testInvalidClientHostCreation() {
        assertThrows(IllegalArgumentException.class,
                () -> new PublishClient(null, BROKER_PORT, CLIENT_ID, TOPIC, mockMessageStream));
    }

    /**
     * 잘못된 포트로 PublishClient 객체를 생성할 때 예외가 발생하는지 테스트합니다.
     */
    @Test
    void testInvalidClientPortCreation() {
        assertThrows(IllegalArgumentException.class,
                () -> new PublishClient(BROKER_HOST, -1, CLIENT_ID, TOPIC, mockMessageStream));
    }

    /**
     * 잘못된 클라이언트 ID로 PublishClient 객체를 생성할 때 예외가 발생하는지 테스트합니다.
     */
    @Test
    void testInvalidClientIdCreation() {
        assertThrows(IllegalArgumentException.class,
                () -> new PublishClient(BROKER_HOST, BROKER_PORT, null, TOPIC, mockMessageStream));
    }

    /**
     * PUBLISH 메시지를 전송할 때 정상적으로 처리되는지 테스트합니다.
     *
     * @throws IOException 입출력 예외가 발생할 경우
     */
    @Test
    void testSendPublish() throws IOException {
        publishClient = spy(publishClient);
        doNothing().when(mockOutput).write(any(byte[].class));

        publishClient.sendPublish(TOPIC, MESSAGE1);

        verify(mockOutput, times(1)).write(any(byte[].class));
        verify(mockOutput, times(1)).flush();

        assertTrue(publishClient.publishMap.size() > 0); // PacketId가 저장되었는지 확인
    }

    /**
     * 정상적인 PUBACK 메시지를 수신하고 처리하는지 테스트합니다.
     *
     * @throws IOException 입출력 예외가 발생할 경우
     */
    @Test
    void testReceivePubAckValid() throws IOException {
        PUBLISH publish = new PUBLISH(TOPIC, MESSAGE1);
        int packetId = publish.getPacketId();
        publishClient.publishMap.put(packetId, publish); // Packet ID 등록

        byte[] validPubAck = new PUBACK(packetId).toByteArray();
        doAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(validPubAck, 0, buffer, 0, validPubAck.length);
            return null;
        }).when(mockInput).readFully(any(byte[].class), anyInt(), anyInt());

        publishClient.receivePubAck(TOPIC);

        assertFalse(publishClient.publishMap.containsKey(packetId)); // PacketId가 제거되었는지 확인
    }

    /**
     * 유효하지 않은 PUBACK 메시지를 수신할 경우 예외가 발생하는지 테스트합니다.
     *
     * @throws IOException 입출력 예외가 발생할 경우
     */
    @Test
    void testReceivePubAckInvalid() throws IOException {
        byte[] invalidPubAck = { 0x00, 0x05 }; // 잘못된 Packet ID
        doAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(invalidPubAck, 0, buffer, 0, invalidPubAck.length);
            return null;
        }).when(mockInput).readFully(any(byte[].class), anyInt(), anyInt());

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> publishClient.receivePubAck(TOPIC));
        assertTrue(exception.getMessage().contains("유효하지 않은 메시지를 수신 하였습니다."));
    }

    /**
     * QoS=1일 때 메시지 전송 후 PUBACK을 정상적으로 처리하는지 테스트합니다.
     *
     * @throws IOException 입출력 예외가 발생할 경우
     */
    @Test
    void testSendAndReceivePubAckQoS1() throws IOException {
        publishClient.setQoS(1); // QoS=1 설정
        publishClient = spy(publishClient);

        doNothing().when(mockOutput).write(any(byte[].class));

        // 정상적인 PUBACK을 받을 수 있도록 설정
        PUBLISH publish = new PUBLISH(TOPIC, MESSAGE2);
        int packetId = publish.getPacketId();
        publishClient.publishMap.put(packetId, publish);

        byte[] validPubAck = new PUBACK(packetId).toByteArray();
        doAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(validPubAck, 0, buffer, 0, validPubAck.length);
            return null;
        }).when(mockInput).readFully(any(byte[].class), anyInt(), anyInt());

        publishClient.sendPublish(TOPIC, MESSAGE2);
        publishClient.receivePubAck(TOPIC);

        assertFalse(publishClient.publishMap.containsKey(packetId)); // Packet ID가 제거되었는지 확인
    }
}
