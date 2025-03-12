import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Objects;


public class HttpResponseImpl implements HttpResponse {
    // HttpResponse를 구현합니다.

    private final Socket socket;
    private DataOutputStream dataOutputStream;
    private String charset = "utf-8";

    public HttpResponseImpl(Socket socket) {
        if (Objects.isNull(socket)) {
            throw new IllegalArgumentException("Invalid: socket is null");
        }

        this.socket = socket;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        // DataOutputStream을 이용해서 구현하세요
        // Java에서 기본 데이터 타입들을 이진 형식으로 출력하는 데 사용되는 클래스 입니다.
        // 예를 들어, 파일이나 네트워크 소켓에 데이터를 효율적으로 저장하거나 전송할 때 유용하게 사용할 수 있습니다.
        // https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/io/DataOutputStream.html
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

        PrintWriter printWriter =  new PrintWriter(dataOutputStream,false, Charset.forName(getCharacterEncoding()));
        return printWriter;
    }

    @Override
    public void setCharacterEncoding(String charset) {
        this.charset = charset;
    }

    @Override
    public String getCharacterEncoding() {
        return this.charset;
    }
}
