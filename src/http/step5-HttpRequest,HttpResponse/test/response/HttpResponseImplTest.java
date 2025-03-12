import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

class HttpResponseImplTest {
    HttpResponse httpResponse;
    @BeforeEach
    void setUp(){
        Socket socket = Mockito.mock(Socket.class);
        httpResponse = new HttpResponseImpl(socket);
    }

    @Test
    @DisplayName("Socket is null")
    void constructor(){
        // socket null check, IllegalArgumentException이 발생 하는지 검증합니다.
        Assertions.assertThrows(IllegalArgumentException.class, ()-> {
            new HttpResponseImpl(null);
        });
    }

    @Test
    @DisplayName("instance of PrintWriter")
    void getWriter() throws IOException {
        Assertions.assertInstanceOf(PrintWriter.class,httpResponse.getWriter());
    }

    @Test
    void setCharacterEncoding() {
        httpResponse.setCharacterEncoding("euc-kr");
        Assertions.assertEquals("euc-kr", httpResponse.getCharacterEncoding());
    }

    @Test
    @DisplayName("default Character Encoding : utf-8")
    void getCharacterEncoding() {
        // default getCharacterEncoding()이 'utf-8'인지 검증합니다.
        Assertions.assertEquals(httpResponse.getCharacterEncoding(), "utf-8");
    }
}