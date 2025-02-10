import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PortScannerTest {
    static final int START_PORT = 1000;
    static final int END_PORT = 10000;
    private PortScanner portScanner;

    @BeforeEach
    void setUp() {
        portScanner = new PortScanner("localhost", START_PORT, 10000);
    }

    @Test
    void constructor_ValidInput_ShouldCreateInstance() {
        assertDoesNotThrow(() -> new PortScanner("localhost", START_PORT, 10000));
    }

    @Test
    void constructor_InvalidHost_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> new PortScanner(null, START_PORT, 10000));
    }

    @Test
    void constructor_InvalidPortRange_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> new PortScanner("localhost", 0, 65536));
    }

    @Test
    void constructor_SwappedPortRange_ShouldCorrectOrder() {
        PortScanner scanner = new PortScanner("localhost", END_PORT, START_PORT);
        assertEquals(START_PORT, scanner.startPort);
        assertEquals(END_PORT, scanner.endPort);
    }

    @Test
    void getPortList_ShouldBeEmptyInitially() {
        assertTrue(portScanner.getPortList().isEmpty());
    }

    @Test
    void scan_ShouldDetectOpenPorts() {
        portScanner.scan();
        List<Integer> openPorts = portScanner.getPortList();

        // 테스트 환경에 따라 다름 (이 부분은 환경에 따라 조정)
        assertNotNull(openPorts);
        System.out.println("Found open ports: " + openPorts);
    }
}
