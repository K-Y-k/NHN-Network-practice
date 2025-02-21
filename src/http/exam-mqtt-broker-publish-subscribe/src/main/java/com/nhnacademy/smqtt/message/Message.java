package com.nhnacademy.smqtt.message;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.reflections.Reflections;

import lombok.extern.slf4j.Slf4j;

// TODO: Message 클래스는 인스턴스 생성이 불가능합니다.
@Slf4j
abstract public class Message {
    /**
     * MQTT 메시지 타입을 나타내는 열거형입니다.
     */
    public enum Type {
        UNKNOWN(0),
        CONNECT(1),
        CONNACK(2),
        PUBLISH(3, true),
        PUBACK(4),
        PUBREC(5),
        PUBREL(6),
        PUBCOMP(7),
        SUBSCRIBE(8, true),
        SUBACK(9),
        UNSUBSCRIBE(10),
        UNSUBACK(11),
        PINGREQ(12),
        PINGRESP(13),
        DISCONNECT(14),
        RESERVED(15);

        private final int value;
        private final boolean qos;

        // TODO: Message.Type을 정의하고 있습니다. 타입의 값과 QoS 사용 여부를 정보로 가질 수 있도록 추가하세요.
        Type(int value) {
            this.value = value;
            this.qos = false;
        }

        Type(int value, boolean qos) {
            this.value = value;
            this.qos = qos;
        }

        int getValue() {
            return this.value;
        }

        boolean applyQoS() {
            return this.qos;
        }
    }

    // TODO: 각 필드에 초기값이 필요한 경우 초기화합니다.
    private static final Map<Type, Class<? extends Message>> messageMap = new ConcurrentHashMap<>();

    static {
        /*
         * Reflections을 이용해 메시지 클래스를 검색해 등록합니다.
         */
        Reflections reflections = new Reflections(Message.class.getPackageName());
        Set<Class<? extends Message>> classes = reflections.getSubTypesOf(Message.class);

        for (Class<? extends Message> clazz : classes) {
            try {
                if (Message.class != clazz) {
                    messageMap.put(Type.valueOf(clazz.getSimpleName()), clazz);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    Type type;
    boolean duplicated;
    int qos;
    boolean retained;

    // TODO: 인스턴스가 불가능한 클래스의 경우, 생성자 접근에 제한을 둡니다.
    /**
     * Message 생성자.
     *
     * @param type 메시지 타입
     */
    protected Message(Type type) {
        this.type = type;
    }

    /**
     * 메시지 타입을 반환합니다.
     *
     * @return 메시지 타입
     */
    public Type getType() {
        return type;
    }

    /**
     * 주어진 타입 값이 현재 메시지 타입과 일치하는지 확인합니다.
     *
     * @param typeValue 타입 값
     * @return 타입이 일치하면 true, 그렇지 않으면 false
     */
    public boolean validate(int typeValue) {
        return type.getValue() == typeValue;
    }

    /**
     * QoS 값을 설정합니다.
     *
     * @param qos QoS 값 (0, 1, 2 중 하나)
     * @throws IllegalArgumentException 잘못된 QoS 값이 전달된 경우
     */
    public void setQoS(int qos) {
        // TODO: 인수를 검증하고 설정합니다. 허용되지 않을 경우, IllegalArgumentException
        if (qos > 2  || qos < 0) {
            throw new IllegalArgumentException("valid : 0 < qos < 3");
        }
        this.qos = qos;
    }

    /**
     * QoS 값을 반환합니다.
     *
     * @return QoS 값
     */
    public int getQoS() {
        return qos;
    }

    /**
     * 메시지의 보존 여부를 설정합니다.
     *
     * @param retained 보존 여부
     */
    public void setRetained(boolean retained) {
        // TODO: 인수를 검증하고 설정합니다.
        this.retained = retained;
    }

    /**
     * 메시지가 보존되는지 여부를 반환합니다.
     *
     * @return 보존 여부
     */
    public boolean isRetained() {
        return retained;
    }

    /**
     * 메시지의 중복 여부를 설정합니다.
     *
     * @param duplicated 중복 여부
     */
    public void setDuplicated(boolean duplicated) {
        this.duplicated = duplicated;
    }

    /**
     * 메시지가 중복되는지 여부를 반환합니다.
     *
     * @return 중복 여부
     */
    public boolean isDuplicated() {
        return duplicated;
    }

    /**
     * 고정 헤더를 반환합니다.
     *
     * @param length 고정 헤더의 길이
     * @return 고정 헤더 바이트 배열
     */
    public byte[] getFixedHeader(int length) {
        // TODO: 메시지 타입에 맞는 고정 헤더를 생성해 반환합니다.
        byte[] fixedHeader = new byte[2];
        fixedHeader[0] = (byte) (this.type.getValue() << 4);
        fixedHeader[1] = (byte) length;

        return fixedHeader;
    }

    /**
     * 주어진 길이의 ByteBuffer를 반환합니다.
     *
     * @param length ByteBuffer의 길이
     * @return ByteBuffer
     * @throws IllegalArgumentException 남은 길이 값이 너무 큰 경우
     */
    public ByteBuffer getByteBuffer(int length) {
        int remainingLength = length;
        byte[] remainingLengthFields = new byte[4];
        int remainingLengthFieldSize = 0;

        remainingLengthFields[remainingLengthFieldSize++] = (byte) (length % 128);
        length = length / 128;
        for (int i = 1; length > 0 && i < 4; i++) {
            remainingLengthFields[remainingLengthFieldSize
                    - 1] = (byte) (0x80 | remainingLengthFields[remainingLengthFieldSize - 1]);
            remainingLengthFields[remainingLengthFieldSize++] = (byte) (length % 128);
            length = length / 128;
        }

        if (length > 0) {
            throw new IllegalArgumentException("Remaining Length 값이 너무 큽니다: " + length);
        }

        ByteBuffer buffer = ByteBuffer.allocate(1 + remainingLengthFieldSize + remainingLength);

        // 1. Fixed Header (CONNACK 패킷)
        buffer.put((byte) ((type.getValue() << 4)
                | (((isDuplicated()) ? 1 : 0) << 3)
                | (type.applyQoS() ? (getQoS() << 1) : 0)
                | ((isRetained()) ? 1 : 0)));

        // 2. Remaining Length (2바이트 고정)
        for (int i = remainingLengthFieldSize - 1; i >= 0; i--) {
            buffer.put(remainingLengthFields[i]);
        }

        return buffer;
    }

    /**
     * 메시지를 바이트 배열로 변환합니다.
     *
     * @return 바이트 배열
     */
    public byte[] toByteArray() {
        return getFixedHeader(0);
    }

    /**
     * 주어진 바이트 배열을 파싱하여 메시지를 생성합니다.
     *
     * @param bytes 바이트 배열
     * @return 생성된 메시지
     * @throws IllegalArgumentException 잘못된 인자가 전달된 경우
     */
    public static Message parsing(byte[] bytes) {
        // TODO: 인수를 검증하고, 메시지를 생성합니다.
        if (bytes == null || bytes.length < 3) {
            throw new IllegalArgumentException();
        }

        Type type = Message.Type.values()[(bytes[0] >> 4) & 0x0F];
        log.debug("type:{}", type);

        // for (Entry<Type, Class<? extends Message>> entry : messageMap.entrySet()) {
        //     log.debug("type:{} / class:{}", entry.getKey(), entry.getValue());
        // }

        if (!messageMap.containsKey(type)) {
            log.warn("유효하지 않은 메시지를 수신 하였습니다: {}", Arrays.toString(bytes));
            throw new IllegalArgumentException("유효하지 않은 메시지를 수신 하였습니다.");
        }

        try {
            log.debug("messageMap.get(type):", messageMap.get(type));

            Class<? extends Message> clazz = messageMap.get(type);
            Constructor<? extends Message> constructor = clazz.getDeclaredConstructor(
                    byte[].class);
            log.debug("class: ", constructor);

            return constructor.newInstance(bytes);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {

            log.warn(e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     * 주어진 바이트 배열을 파싱하여 메시지를 생성합니다.
     *
     * @param bytes  바이트 배열
     * @param offset 오프셋
     * @param length 길이
     * @return 생성된 메시지
     * @throws IllegalArgumentException 잘못된 인자가 전달된 경우
     */
    public static Message parsing(byte[] bytes, int offset, int length) {
        if ((bytes == null) || (length < 2) || (bytes.length < offset + length)) {
            throw new IllegalArgumentException();
        }

        int remainingLengthFieldSize = 0;
        int remainingLength = 0;
        /*
         * TODO: remaining length와 remaining length를 위해 사용된 바이트 수를 계산합니다.
         */
        

        if (length < (1 + remainingLengthFieldSize + remainingLength)) {
            throw new IllegalArgumentException();
        }

        Type type = Message.Type.values()[(bytes[offset] >> 4) & 0x0F];

        if (!messageMap.containsKey(type)) {
            log.warn("유효하지 않은 메시지를 수신 하였습니다: {}", Arrays.toString(bytes));
            throw new IllegalArgumentException("유효하지 않은 메시지를 수신 하였습니다.");
        }

        try {
            Class<? extends Message> clazz = messageMap.get(type);
            Constructor<? extends Message> constructor = clazz.getDeclaredConstructor(
                    byte[].class,
                    Integer.class,
                    Integer.class);

            return constructor.newInstance(bytes, offset + remainingLengthFieldSize + 1, remainingLength);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {

            log.warn(e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     * 메시지 타입을 문자열로 반환합니다.
     *
     * @return 메시지 타입 문자열
     */
    @Override
    public String toString() {
        return type.toString();
    }
}
