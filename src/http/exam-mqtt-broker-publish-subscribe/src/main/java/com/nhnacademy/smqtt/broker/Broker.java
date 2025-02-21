package com.nhnacademy.smqtt.broker;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import com.nhnacademy.smqtt.message.CONNACK;
import com.nhnacademy.smqtt.message.CONNECT;
import com.nhnacademy.smqtt.message.Message;
import com.nhnacademy.smqtt.message.PINGRESP;
import com.nhnacademy.smqtt.message.PUBLISH;
import com.nhnacademy.smqtt.message.SUBACK;
import com.nhnacademy.smqtt.message.SUBSCRIBE;
import com.nhnacademy.smqtt.message.UNSUBSCRIBE;

import lombok.extern.slf4j.Slf4j;

/**
 * MQTT Broker 클래스는 클라이언트와의 연결을 관리하고 메시지를 주고받는 역할을 합니다.
 * 이 클래스는 스레드를 상속받아 여러 클라이언트와 동시에 통신할 수 있습니다.
 */
@Slf4j
public class Broker extends Thread {

    private final Map<String, List<ClientHandler>> topicSubscribers = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private int port;

    /**
     * Broker 생성자.
     *
     * @param port 브로커가 수신 대기할 포트 번호
     */
    public Broker(int port) {
        // TODO: 수정하세요.
        this.port = port;
    }

    /**
     * 브로커를 시작하여 클라이언트 연결을 수신 대기합니다.
     * 클라이언트가 연결되면 ClientHandler를 통해 처리합니다.
     */
    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log.info("MQTT Broker started on port: {}", port);

            while (!Thread.currentThread().isInterrupted()) {
                Socket clientSocket = serverSocket.accept();
                executorService.submit(new ClientHandler(clientSocket));
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않음 포트입니다: " + port);
        } catch (IOException e) {
            log.warn(e.getMessage());
        } finally {
            executorService.shutdownNow();
        }
    }

    /**
     * 클라이언트를 특정 토픽에 구독시킵니다.
     *
     * @param client 구독할 클라이언트
     * @param topic  구독할 토픽
     */
    public void subscribe(ClientHandler client, String topic) {
        topicSubscribers.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>()).add(client);
        log.info("Client subscribed to topic: {}", topic);
    }

    /**
     * 클라이언트를 특정 토픽에서 구독 해제합니다.
     *
     * @param client 구독 해제할 클라이언트
     * @param topic  구독 해제할 토픽
     */
    public void unsubscribe(ClientHandler client, String topic) {
        topicSubscribers.getOrDefault(topic, new ArrayList<>()).remove(client);
        log.info("Client unsubscribed from topic: {}", topic);
    }

    /**
     * 특정 토픽에 메시지를 발행합니다.
     * 해당 토픽을 구독 중인 모든 클라이언트에게 메시지를 전송합니다.
     *
     * @param topic   메시지를 발행할 토픽
     * @param message 발행할 메시지
     */
    public void publish(String topic, String message) {
        List<ClientHandler> subscribers = topicSubscribers.get(topic);
        if (subscribers != null) {
            for (ClientHandler subscriber : subscribers) {
                subscriber.sendPublish(topic, message);
            }
        }
    }

    /**
     * 클라이언트와의 연결을 처리하는 클래스입니다.
     * 각 클라이언트는 별도의 스레드에서 처리됩니다.
     */
    class ClientHandler implements Runnable {
        private final Socket socket;
        private DataInputStream input;
        private DataOutputStream output;
        private boolean connected = false;
        private int remotePort;

        /**
         * ClientHandler 생성자.
         *
         * @param socket 클라이언트와의 소켓 연결
         * @throws IOException 입출력 예외 발생 시
         */
        public ClientHandler(Socket socket) throws IOException {
            if (socket == null) {
                throw new IllegalArgumentException();
            }

            remotePort = socket.getPort();

            this.socket = socket;
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
        }

        /**
         * 클라이언트로부터 메시지를 수신하고 처리합니다.
         * 메시지 유형에 따라 적절한 응답을 전송합니다.
         */
        @Override
        public void run() {
            log.info("Start : {}", remotePort);
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Message message = receive();
                    log.info("Received: {}", message.toString());

                    switch (message.getType()) {
                        case CONNECT: {
                            // TODO: CONNECT 메시지를 확인하고, CONNACK를 반환합니다.
                            new CONNACK(message.toByteArray());
                            break;
                        }

                        case PUBLISH: {
                            // TODO: PUBLISH 메시지를 확인하고, subscriber에게 해당 메시지를 전송합니다.
                            send(new PUBLISH(message.toByteArray()));
                        }

                        case SUBSCRIBE: {
                            // TODO: SUBSCRIBE 메시지를 확인하고, 토픽 등록 후 SUBACK를 전송합니다.
                            send(new SUBACK(message.toByteArray()));
                            break;
                        }

                        case UNSUBSCRIBE: {
                            // TODO: UNSUBSCRIBE 메시지를 확인하고, 해당 토픽을 제거 합니다.
                            
                            break;
                        }

                        case PINGREQ: {
                            // TODO: PINGREG 메시지를 확인하고, PINGRESP를 전송합니다.
                            break;
                        }

                        case DISCONNECT: {
                            log.info("Client disconnected.");
                            socket.close();
                            Thread.currentThread().interrupt();
                            break;
                        }

                        default: {
                            log.warn("지원하지 않는 명령입니다: {}", message.toString());
                        }
                    }
                }
            } catch (IOException e) {
                log.warn(e.getMessage());
                Thread.currentThread().interrupt();
            } finally {
                closeConnection();
                log.info("Finished : {}", remotePort);
            }
        }

        /**
         * 클라이언트에게 발행 메시지를 전송합니다.
         *
         * @param topic   발행할 토픽
         * @param message 발행할 메시지
         */
        public void sendPublish(String topic, String message) {
            try {
                output.write(new PUBLISH(topic, message).toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * 클라이언트와의 연결을 종료합니다.
         * 소켓 및 스트림을 닫습니다.
         */
        void closeConnection() {
            try {
                log.info("Close Connection: {}", remotePort);
                if (socket != null) {
                    socket.close();
                }

                if (input != null) {
                    input.close();
                }
                if (output != null) {
                    output.close();
                }
            } catch (IOException ignore) {
                throw new RuntimeException("close Connection exception");
            }
            log.info("Disconnected: {}", remotePort);
        }

        /**
         * 클라이언트로부터 메시지를 수신합니다.
         * 메시지의 헤더와 본문을 읽어 파싱합니다.
         *
         * @return 수신한 메시지 객체
         * @throws IOException 입출력 예외 발생 시
         */
        protected synchronized Message receive() throws IOException {
            byte[] header = new byte[5];
            input.readFully(header, 0, 2);

            int lengthFieldCount = 1;
            int length = header[lengthFieldCount] & 0x7F;
            while ((lengthFieldCount < 4) && ((header[lengthFieldCount] & 0x80) == 0x80)) {
                lengthFieldCount++;
                input.readFully(header, lengthFieldCount, 1);
                length += (int) ((header[lengthFieldCount] & 0x7F) * Math.pow(128, lengthFieldCount - 1.0));
            }

            byte[] buffer = Arrays.copyOf(header, 1 + lengthFieldCount + length);
            input.readFully(buffer, 1 + lengthFieldCount, length);

            return Message.parsing(buffer);
        }

        /**
         * 클라이언트에게 메시지를 전송합니다.
         *
         * @param message 전송할 메시지 객체
         */
        public void send(Message message) {
            try {
                output.write(message.toByteArray());
                output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
