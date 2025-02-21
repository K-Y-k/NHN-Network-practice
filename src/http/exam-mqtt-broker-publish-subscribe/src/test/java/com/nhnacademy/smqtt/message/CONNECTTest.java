package com.nhnacademy.smqtt.message;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class CONNECTTest {

    /**
     * 유효한 Client ID를 사용하여 CONNECT 객체를 생성하고, Client ID가 올바르게 설정되었는지 확인합니다.
     */
    @Test
    void testValidClientId() {
        CONNECT connect = new CONNECT("validClient123");
        assertEquals("validClient123", connect.getClientId());
    }

    /**
     * 너무 긴 Client ID를 사용하여 CONNECT 객체를 생성할 때 예외가 발생하는지 확인합니다.
     */
    @Test
    void testInvalidClientIdTooLong() {
        // 23자 초과
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new CONNECT("ThisClientIdIsWayTooLongForMQTT"));
        assertEquals("Client ID는 1~23자 이내여야 합니다.", exception.getMessage());
    }

    /**
     * 빈 Client ID를 사용하여 CONNECT 객체를 생성할 때 예외가 발생하는지 확인합니다.
     */
    @Test
    void testInvalidClientIdEmpty() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new CONNECT(""));
        assertEquals("Client ID는 1~23자 이내여야 합니다.", exception.getMessage());
    }

    /**
     * 유효한 CONNECT 메시지를 직렬화하고 다시 역직렬화하여 데이터가 올바르게 유지되는지 확인합니다.
     */
    @Test
    void testValidCONNECTMessageSerialization() {
        CONNECT connect = new CONNECT("Client123");
        connect.setUserName("testUser");
        connect.setPassword("testPass");
        connect.setCleanSession(true);
        connect.setKeepAlive((short) 45);


        byte[] serialized = connect.toByteArray();
        CONNECT deserialized = new CONNECT(serialized, 2, serialized.length - 2);

        assertEquals("Client123", deserialized.getClientId());
        assertEquals("testUser", deserialized.getUserName());
        assertEquals("testPass", deserialized.getPassword());
        assertTrue(deserialized.getCleanSession());
        assertEquals(45, deserialized.getKeepAlive());
    }

    /**
     * 잘못된 크기의 CONNECT 페이로드를 사용하여 객체를 생성할 때 예외가 발생하는지 확인합니다.
     */
    @Test
    void testInvalidCONNECTPayload() {
        byte[] invalidPayload = new byte[2]; // 잘못된 크기
        assertThrowsExactly(IllegalArgumentException.class,
                () -> new CONNECT(invalidPayload));
    }

    /**
     * 잘못된 Protocol Name을 사용하여 CONNECT 객체를 생성할 때 예외가 발생하는지 확인합니다.
     */
    @Test
    void testInvalidProtocolName() {
        ByteBuffer buffer = ByteBuffer.allocate(20);
        buffer.putShort((short) 4); // 잘못된 Protocol 길이
        buffer.put("FAIL".getBytes()); // 잘못된 Protocol Name
        buffer.put((byte) 4);
        buffer.put((byte) 0x02); // Clean Session
        buffer.putShort((short) 60);
        buffer.putShort((short) 5);
        buffer.put("Valid".getBytes());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new CONNECT(buffer.array()));
        assertEquals("Protocol Name이 'MQTT'가 아닙니다.", exception.getMessage());
    }

    /**
     * 잘못된 Protocol Level을 설정할 때 예외가 발생하는지 확인합니다.
     */
    @Test
    void testSetInvalidProtocolLevel() {
        CONNECT connect = new CONNECT("Client123");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> connect.setProtocolLevel((byte) 6));
        assertEquals("Protocol Level 값이 올바르지 않습니다.", exception.getMessage());
    }

    /**
     * User Name을 설정하고 올바르게 설정되었는지 확인합니다.
     */
    @Test
    void testUserNameSetter() {
        CONNECT connect = new CONNECT("ClientTest");
        connect.setUserName("NewUser");
        assertEquals("NewUser", connect.getUserName());
    }

    /**
     * User Name을 null로 설정할 때 예외가 발생하는지 확인합니다.
     */
    @Test
    void testSetUserNameNull() {
        CONNECT connect = new CONNECT("ClientTest");
        assertThrows(IllegalArgumentException.class, () -> connect.setUserName(null));
    }

    /**
     * Password를 null로 설정할 때 예외가 발생하는지 확인합니다.
     */
    @Test
    void testSetPasswordNull() {
        CONNECT connect = new CONNECT("ClientTest");
        assertThrows(IllegalArgumentException.class, () -> connect.setPassword(null));
    }

    /**
     * Keep Alive 값을 설정하고 올바르게 설정되었는지 확인합니다.
     */
    @Test
    void testKeepAliveSetter() {
        CONNECT connect = new CONNECT("ClientTest");
        connect.setKeepAlive((short) 120);
        assertEquals(120, connect.getKeepAlive());
    }

    /**
     * Clean Session 값을 설정하고 올바르게 설정되었는지 확인합니다.
     */
    @Test
    void testCleanSessionSetter() {
        CONNECT connect = new CONNECT("ClientTest");
        connect.setCleanSession(true);
        assertTrue(connect.getCleanSession());

        connect.setCleanSession(false);
        assertFalse(connect.getCleanSession());
    }

    /**
     * 정상적인 바이트 배열을 사용하여 객체 생성 테스트
     */
    @Test
    void testValidPayload() {
        byte[] payload = createValidConnectPayload("Client123", "", "");
        CONNECT connect = new CONNECT(payload, 0, payload.length);
        assertEquals("Client123", connect.getClientId());
        assertEquals("", connect.getUserName());
        assertEquals("", connect.getPassword());
    }

    /**
     * 너무 짧은 바이트 배열을 입력할 경우 예외 발생
     */
    @Test
    void testInvalidShortPayload() {
        byte[] invalidPayload = new byte[2]; // 너무 짧음
        assertThrows(IllegalArgumentException.class, () -> new CONNECT(invalidPayload, 0, invalidPayload.length));
    }

    /**
     * offset과 length가 잘못된 경우 예외 발생
     */
    @Test
    void testInvalidOffsetLength() {
        byte[] payload = createValidConnectPayload("Client123", "", "");
        assertThrows(IllegalArgumentException.class, () -> new CONNECT(payload, -1, payload.length));
        assertThrows(IllegalArgumentException.class, () -> new CONNECT(payload, 0, payload.length + 10));
    }

    /**
     * Protocol Level이 올바르지 않은 경우 예외 발생
     */
    @Test
    void testInvalidProtocolLevel() {
        byte[] payload = createValidConnectPayload("Client123", "", "");
        payload[6] = 0x06; // 잘못된 Protocol Level (MQTT 3.1.1 = 0x04, MQTT 5.0 = 0x05)
        assertThrows(IllegalArgumentException.class, () -> new CONNECT(payload, 0, payload.length));
    }

    /**
     * 유효하지 않은 Client ID (길이 초과 또는 비어 있음)
     */
    @Test
    void testInvalidClientId() {
        byte[] longClientIdPayload = createValidConnectPayload("ThisClientIdIsWayTooLongForMQTT", "", "");
        assertThrows(IllegalArgumentException.class,
                () -> new CONNECT(longClientIdPayload, 0, longClientIdPayload.length));

        byte[] emptyClientIdPayload = createValidConnectPayload("", "", "");
        assertThrows(IllegalArgumentException.class,
                () -> new CONNECT(emptyClientIdPayload, 0, emptyClientIdPayload.length));
    }

    /**
     * User Name 필드가 존재하는 경우 정상적으로 파싱되는지 확인
     */
    @Test
    void testValidUserNamePayload() {
        byte[] payload = createValidConnectPayload("Client123", "testUser", "");
        CONNECT connect = new CONNECT(payload, 0, payload.length);
        assertEquals("Client123", connect.getClientId());
        assertEquals("testUser", connect.getUserName());
    }

    /**
     * Password 필드가 존재하는 경우 정상적으로 파싱되는지 확인
     */
    @Test
    void testValidPasswordPayload() {
        byte[] payload = createValidConnectPayload("Client123", "testUser", "testPass");
        CONNECT connect = new CONNECT(payload, 0, payload.length);
        assertEquals("Client123", connect.getClientId());
        assertEquals("testUser", connect.getUserName());
        assertEquals("testPass", connect.getPassword());
    }

    /**
     * 정상적인 CONNECT 패킷을 생성하는 헬퍼 메서드
     */
    private byte[] createValidConnectPayload(String clientId, String userName, String password) {
        byte[] protocolBytes = "MQTT".getBytes(StandardCharsets.UTF_8);
        byte[] clientIdBytes = clientId.getBytes(StandardCharsets.UTF_8);
        byte[] userNameBytes = userName.isEmpty() ? new byte[0] : userName.getBytes(StandardCharsets.UTF_8);
        byte[] passwordBytes = password.isEmpty() ? new byte[0] : password.getBytes(StandardCharsets.UTF_8);

        int remainingLength = 2 + protocolBytes.length + 1 + 1 + 2 + 2 + clientIdBytes.length;
        if (!userName.isEmpty()) {
            remainingLength += 2 + userNameBytes.length;
        }
        if (!password.isEmpty()) {
            remainingLength += 2 + passwordBytes.length;
        }

        ByteBuffer buffer = ByteBuffer.allocate(remainingLength);
        buffer.putShort((short) protocolBytes.length);
        buffer.put(protocolBytes);
        buffer.put((byte) 0x04); // Protocol Level (MQTT 3.1.1)
        
        byte connectFlags = 0x02; // Clean Session
        if (!userName.isEmpty()) {
            connectFlags |= 0x80;
        }
        if (!password.isEmpty()) {
            connectFlags |= 0x40;
        }
        buffer.put(connectFlags);
        buffer.putShort((short) 60); // Keep Alive

        buffer.putShort((short) clientIdBytes.length);
        buffer.put(clientIdBytes);

        if (!userName.isEmpty()) {
            buffer.putShort((short) userNameBytes.length);
            buffer.put(userNameBytes);
        }

        if (!password.isEmpty()) {
            buffer.putShort((short) passwordBytes.length);
            buffer.put(passwordBytes);
        }

        return buffer.array();
    }

}