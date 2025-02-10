import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * 포트 스캐너를 구현하는 클래스
 */
public class PortScanner {
    static final int MIN_PORT = 1;
    static final int MAX_PORT = 65535;

    String host;
    int startPort = MIN_PORT;
    int endPort = MAX_PORT;
    List<Integer> portList = new LinkedList<>();

    /**
     * PortScanner 클래스의 생성자입니다.
     *
     * @param host      호스트
     * @param startPort 포트 스캐닝을 시작할 포트
     * @param endPort   포트 스캐닝을 끝낼 포트
     */
    public PortScanner(String host, int startPort, int endPort) {
        if (startPort > endPort) {
            int temp = startPort;
            startPort = endPort;
            endPort = temp;
        }
        if ((host == null) || (startPort < MIN_PORT) || (MAX_PORT < endPort)) {
            throw new IllegalArgumentException();
        }

        this.host = host;
        this.startPort = startPort;
        this.endPort = endPort;
    }

    /**
     * 현재 포트 목록을 반환합니다.
     *
     * @return 포트 목록
     */
    public List<Integer> getPortList() {
        return portList;
    }

    /**
     * 설정된 호스트와 포트 범위에 대해 포트 스캐닝을 수행합니다.
     *
     */
    public void scan() {

        portList.clear();

        for (int port = startPort; port <= endPort; port++) {
            // try-with-resources를 이용해 소켓 생성하고, 정상적으로 연결될 경우 포트를 저장합니다.
            try (Socket socket = new Socket(host, port)) {
                portList.add(port);
            } catch (IOException ignore) {
            }
            
            // 소켓을 닫지 않은 상태로 끝낼 경우
            // GC가 언제 수행될지 명확하지 않아 적절한 시점에서의 자원 회수가 되지 않을 수 있습니다.
            // try {
            //     Socket socket = new Socket(this.host, port);
                
            //     if (socket.isConnected()) {
            //         this.portList.add(port);
            //     }

            // } catch (IOException ignore) {
            // }
        }
    }

    /**
     * Main 메소드는 포트 스캐너를 실행하는 메소드입니다.
     *
     * @param args 명령행 인수
     */
    public static void main(String[] args) {
        String host = "localhost";
        int startPort = 1000;
        int endPort = 10000;
        Scanner terminalIn = new Scanner(System.in);

        PortScanner portScanner = new PortScanner(host, startPort, endPort);
        portScanner.scan();

        if (!portScanner.getPortList().isEmpty()) {
            for (Integer port : portScanner.getPortList()) {
                System.out.printf("포트[%5d]가 열려 있습니다.%n", port);
            }
        } else {
            System.out.printf("요청하신 범위[%d ~ %d]내에 열려 있는 포트가 없습니다.%n", startPort, endPort);
        }

        System.out.println("터미널에서 소켓 연결 상태를 확인하세요. 엔터를 입력하시면 종료합니다.");
        terminalIn.nextLine();
        terminalIn.close();
    }
}
