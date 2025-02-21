package com.nhnacademy.smqtt.client;

import com.nhnacademy.smqtt.message.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class ClientTest {
    private static final String CLIENT_ID = "ClientTest";
    private static final String EXTERNAL_BROKER_HOST = "test.mosquitto.org";
    private static final int BROKER_PORT = 1883;
    private Socket mockSocket;
    private DataInputStream mockInput;
    private DataOutputStream mockOutput;
    private Client mockClient;

    /**
     * 각 테스트 전에 실행되어 Mock 객체를 초기화하고 Client 인스턴스를 생성합니다.
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

        // Client를 추상 클래스로 Mocking
        mockClient = new Client(mockSocket, mockInput, mockOutput, CLIENT_ID) {
        };
    }

    /**
     * Client 객체가 정상적으로 생성되는지 테스트합니다.
     */
    @Test
    void testValidClientCreation() {
        assertDoesNotThrow(() -> new Client(EXTERNAL_BROKER_HOST, BROKER_PORT, CLIENT_ID) {
        });
    }

    /**
     * Client 객체 생성 시 잘못된 인자가 전달될 경우 예외가 발생하는지 테스트합니다.
     */
    @Test
    void testInvalidClientCreation() {
        assertThrows(IllegalArgumentException.class, () -> new Client(null, BROKER_PORT, CLIENT_ID) {
        });

        assertThrows(IllegalArgumentException.class, () -> new Client(EXTERNAL_BROKER_HOST, -1, CLIENT_ID) {
        });

        assertThrows(IllegalArgumentException.class, () -> new Client(EXTERNAL_BROKER_HOST, BROKER_PORT, null) {
        });
    }

    /**
     * MQTT CONNECT 메시지를 전송할 때 정상적으로 처리하는지 테스트합니다.
     *
     * @throws IOException 입출력 예외가 발생할 경우
     */
    @Test
    void testSendConnect() throws IOException {
        doNothing().when(mockOutput).write(any(byte[].class));

        mockClient.sendConnect();

        verify(mockOutput, times(1)).write(any(byte[].class));
        verify(mockOutput, times(1)).flush();
    }

    /**
     * 정상적인 CONNACK 메시지를 수신하고 처리하는지 테스트합니다.
     *
     * @throws IOException 입출력 예외가 발생할 경우
     */
    @Test
    void testReceiveConnAckValid() throws IOException {
        byte[] validConnAck = new CONNACK(false, 0).toByteArray();
        doAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(validConnAck, 0, buffer, 0, validConnAck.length);
            return null;
        }).when(mockInput).readFully(any(byte[].class), anyInt(), anyInt());

        assertDoesNotThrow(() -> mockClient.receiveConnAck());
    }

    /**
     * 유효하지 않은 CONNACK 메시지를 수신할 경우 예외가 발생하는지 테스트합니다.
     *
     * @throws IOException 입출력 예외가 발생할 경우
     */
    @Test
    void testReceiveConnAckInvalid() throws IOException {
        byte[] invalidConnAck = { 0x00, 0x07 }; // Return Code = 7 (잘못된 값)
        doAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(invalidConnAck, 0, buffer, 0, invalidConnAck.length);
            return null;
        }).when(mockInput).readFully(any(byte[].class), anyInt(), anyInt());

        Exception exception = assertThrows(IllegalArgumentException.class, mockClient::receiveConnAck);
        assertTrue(exception.getMessage().contains("유효하지 않은 메시지를 수신 하였습니다."));
    }

    /**
     * QoS 설정 및 검증을 테스트합니다.
     */
    @Test
    void testQoSSettings() {
        mockClient.setQoS(1);
        assertEquals(1, mockClient.getQoS());

        assertThrows(IllegalArgumentException.class, () -> mockClient.setQoS(3));
        assertThrows(IllegalArgumentException.class, () -> mockClient.setQoS(-1));
    }

    /**
     * DISCONNECT 메시지를 전송할 때 정상적으로 처리하는지 테스트합니다.
     *
     * @throws IOException 입출력 예외가 발생할 경우
     */
    @Test
    void testSendDisconnect() throws IOException {
        doNothing().when(mockOutput).write(any(byte[].class));

        mockClient.sendDisconnect();

        verify(mockOutput, times(1)).write(any(byte[].class));
        verify(mockOutput, times(1)).flush();
    }

    /**
     * 메시지를 수신할 때 정상적으로 처리하는지 테스트합니다.
     *
     * @throws IOException 입출력 예외가 발생할 경우
     */
    @Test
    void testReceiveMessage() throws IOException {
        AtomicInteger offset = new AtomicInteger();
        byte[] validMessage = new CONNECT(CLIENT_ID).toByteArray();
        doAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(validMessage, offset.get(), buffer, invocation.getArgument(1), invocation.getArgument(2));
            offset.set(offset.get() + (Integer) invocation.getArgument(2));

            return buffer;
        }).when(mockInput).readFully(any(byte[].class), anyInt(), anyInt());

        Message receivedMessage = mockClient.receive();
        assertNotNull(receivedMessage);
    }

    /**
     * 연결을 종료할 때 정상적으로 처리하는지 테스트합니다.
     *
     * @throws IOException 입출력 예외가 발생할 경우
     */
    @Test
    void testCloseConnection() throws IOException {
        mockClient.closeConnection();

        verify(mockInput, times(1)).close();
        verify(mockOutput, times(1)).close();
        verify(mockSocket, times(1)).close();
    }

    /**
     * 패킷 ID가 올바르게 생성되는지 테스트합니다.
     */
    @Test
    void testGetPacketId() {
        int firstPacketId = Client.getPacketId();
        int secondPacketId = Client.getPacketId();

        assertEquals(firstPacketId + 1, secondPacketId);
    }
}
