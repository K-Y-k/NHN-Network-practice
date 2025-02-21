package com.nhnacademy.smqtt.message;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CONNECT extends Message {
    private static final String PROTOCOL_ID = "MQTT";
    private final String clientId;
    private byte protocolLevel = 4;
    private boolean cleanSession = false;
    private short keepAlive = 60;
    private String userName = "";
    private String password = "";

    /**
     * clientId를 직접 받는 생성자
     *
     * @param clientId 클라이언트 식별자
     * @throws IllegalArgumentException clientId가 null이거나 비어있거나 길이가 23자를 초과하는 경우
     */
    public CONNECT(String clientId) {
        super(Type.CONNECT);

        // TODO: 인수를 검증하고, 필드를 설정합니다.
        if (clientId.length() > 23 || clientId.length() < 1) {
            throw new IllegalArgumentException("Client ID는 1~23자 이내여야 합니다.");
        }

        this.clientId = clientId;
    }

    /**
     * 바이트 배열을 받아서 CONNECTRequest 객체 생성
     *
     * @param data   바이트 배열로 표현된 CONNECT 메시지(Fixed Header를 제외한 나머지)
     * @param offset 바이트 배열에서 메시지가 시작되는 위치
     * @param length 메시지의 길이
     * @throws IllegalArgumentException payload가 null이거나, offset이 음수이거나, length가 0보다 작거나, payload의 길이가 offset + length보다 작을 경우
     */
    public CONNECT(byte[] data, Integer offset, Integer length) {
        super(Type.CONNECT);

        // TODO: 인수를 검증하고, 필드를 설정합니다.
        if (data == null || offset < 0 || length < 0 || data.length < offset + length) {
            throw new IllegalArgumentException("invalid : payload.length or offset or length");
        }

        ByteBuffer buffer = ByteBuffer.wrap(data, offset, length);

        try {
            // 1. Protocol Name 검사
            byte[] protocolBytes = new byte[buffer.getShort() & 0xFFFF];
            buffer.get(protocolBytes);

            log.debug("PROTOCOL_ID:{}", new String(protocolBytes));
            if (!PROTOCOL_ID.equals(new String(protocolBytes))) {
                throw new IllegalArgumentException("Protocol Name이 'MQTT'가 아닙니다.");
            }

            // 2. Protocol Level 검사 (MQTT 3.1.1 = 0x04, MQTT 5.0 = 0x05)
            byte byteProtocolLevel = buffer.get();

            if (byteProtocolLevel != 0x04 && byteProtocolLevel != 0x05) {
                throw new BufferUnderflowException();
            }
            this.protocolLevel = byteProtocolLevel;
            log.debug("byteProtocolLevel:{}", byteProtocolLevel);

            // 3. Connect Flags 및 Keep Alive 읽기
            byte flags = buffer.get();
            boolean connectFlag = (flags & (1 << 7)) != 0;
            this.cleanSession = connectFlag;
            log.debug("connectFlag:{}", connectFlag);
            
            this.keepAlive = (short) buffer.getShort();
            log.debug("keepAlive:{}", this.keepAlive);

            // 4. Client ID 읽기
            short clientIdLength = buffer.getShort();
            if (clientIdLength > 23 || clientIdLength < 1) {
                throw new IllegalArgumentException("Client ID는 1~23자 이내여야 합니다.");
            }
            byte[] clientIdBytes = new byte[clientIdLength & 0xFFFF];
            buffer.get(clientIdBytes);

            this.clientId = new String(clientIdBytes);
            log.debug("clientId:{}", this.clientId);

            
            if (buffer.hasRemaining()) {
                // 유저 이름
                byte[] userNameBytes = new byte[buffer.getShort() & 0xFFFF];
                buffer.get(userNameBytes);

                this.setUserName(new String(userNameBytes));
                log.debug("userName:{}", this.userName);


                if (buffer.hasRemaining()) {
                    // 패스워드
                    byte[] passwordBytes = new byte[buffer.getShort() & 0xFFFF];
                    buffer.get(passwordBytes);

                    this.setPassword(new String(passwordBytes));
                    log.debug("password:{}", this.password);
                }
            }

        } catch (BufferUnderflowException e) {
            log.warn(e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     * 바이트 배열을 받아서 CONNECTRequest 객체 생성
     *
     * @param payload 바이트 배열로 표현된 CONNECT 메시지
     */
    public CONNECT(byte[] payload) {
        this(payload, 0, (payload != null) ? payload.length : 0);
    }

    /**
     * Client ID 반환
     *
     * @return clientId 클라이언트 식별자
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Protocol Level 설정
     *
     * @param protocolLevel 프로토콜 레벨 (MQTT 3.1.1 = 0x04, MQTT 5.0 = 0x05)
     * @throws IllegalArgumentException protocolLevel 값이 0x04 또는 0x05가 아닌 경우
     */
    public void setProtocolLevel(byte protocolLevel) {
        // TODO: 인수를 검증하세요.
        if (protocolLevel != 0x04 && protocolLevel != 0x05) {
            throw new IllegalArgumentException("Protocol Level 값이 올바르지 않습니다.");
        }

        this.protocolLevel = protocolLevel;
    }

    /**
     * Protocol Level 반환
     *
     * @return protocolLevel 프로토콜 레벨
     */
    public byte getProtocolLevel() {
        return protocolLevel;
    }

    /**
     * Connect Flags 반환
     *
     * @return flags Connect Flags
     */
    private byte getConnectFlags() {
        byte flags = 0;

        if (!userName.isEmpty()) {
            flags |= (1 << 7);
        }

        if (!password.isEmpty()) {
            flags |= (1 << 6);
        }

        if (cleanSession) {
            flags |= (1 << 1);
        }

        return flags;
    }

    /**
     * User Name 설정
     *
     * @param userName 사용자 이름
     * @throws IllegalArgumentException userName이 null인 경우
     */
    public void setUserName(String userName) {
        // TODO: user name을 설정하세요.
        if (Objects.isNull(userName)) {
            throw new IllegalArgumentException("invalid: username is null");
        }

        this.userName = userName;
    }

    /**
     * User Name 반환
     *
     * @return userName 사용자 이름
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Password 설정
     *
     * @param password 비밀번호
     * @throws IllegalArgumentException password가 null인 경우
     */
    public void setPassword(String password) {
        // TODO: password을 설정하세요.
        if (Objects.isNull(password)) {
            throw new IllegalArgumentException("invalid: password is null");
        }

        this.password = password;
    }

    /**
     * Password 반환
     *
     * @return password 비밀번호
     */
    public String getPassword() {
        return password;
    }

    /**
     * Clean Session 설정
     *
     * @param cleanSession 클린 세션 여부
     */
    public void setCleanSession(boolean cleanSession) {
        // TODO: cleanSession을 설정하세요.
        this.cleanSession = cleanSession;
    }

    /**
     * Clean Session 반환
     *
     * @return cleanSession 클린 세션 여부
     */
    public boolean getCleanSession() {
        return cleanSession;
    }

    /**
     * Keep Alive 설정
     *
     * @param keepAlive Keep Alive 시간 (초)
     */
    public void setKeepAlive(short keepAlive) {
        // TODO: 인수를 검증하고, keepAlive를 설정하세요.
        if (keepAlive < 0) {
            throw new IllegalArgumentException("invalid: keepAlive < 0");
        }

        this.keepAlive = keepAlive;
    }

    /**
     * Keep Alive 반환
     *
     * @return keepAlive Keep Alive 시간 (초)
     */
    public short getKeepAlive() {
        return keepAlive;
    }

    /**
     * 현재 객체를 MQTT CONNECT 메시지 형식의 바이트 배열로 변환
     *
     * @return MQTT CONNECT 메시지 형식의 바이트 배열
     */
    @Override
    public byte[] toByteArray() {
        byte[] protocolBytes = PROTOCOL_ID.getBytes(StandardCharsets.UTF_8);
        byte[] clientIdBytes = clientId.getBytes(StandardCharsets.UTF_8);
        byte[] userNameBytes = userName.getBytes(StandardCharsets.UTF_8);
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);

        //           프로토콜 길이  프로토콜명  프로토콜레벨 플래그 킾얼라이브 클라이언트아이디길이 클라이언트아이디명
        int remainingLength = 2 + protocolBytes.length + 1 + 1 + 2 + 2 + clientIdBytes.length;
        if (userNameBytes.length != 0) {
            remainingLength += 2 + userNameBytes.length;
        }

        if (passwordBytes.length != 0) {
            remainingLength += 2 + passwordBytes.length;
        }

        ByteBuffer buffer = getByteBuffer(remainingLength);

        // 1. Protocol Name ("MQTT")
        buffer.putShort((short) protocolBytes.length);
        buffer.put(protocolBytes);

        // 2. Protocol Level (MQTT 3.1.1)
        buffer.put(protocolLevel);

        // 3. Connect Flags (Clean Session 활성화)
        buffer.put(getConnectFlags());

        // 4. Keep Alive
        buffer.putShort(getKeepAlive());

        // 5. Client ID
        buffer.putShort((short) clientIdBytes.length);
        buffer.put(clientIdBytes);

        // 6. User Name
        if (!userName.isEmpty()) {
            buffer.putShort((short) userNameBytes.length);
            buffer.put(userNameBytes);
        }

        // 7. Password Name
        if (!password.isEmpty()) {
            buffer.putShort((short) passwordBytes.length);
            buffer.put(passwordBytes);
        }

        // 현재 버퍼 포지션을 통해 총 길이를 가져옴
        int position = buffer.position();

        byte[] bytes = new byte[buffer.position()];
        buffer.rewind();
        buffer.get(bytes, 0, position);

        log.debug(Arrays.toString(bytes));

        return bytes;
    }
}
