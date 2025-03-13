import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;


public class HttpResponseImpl implements HttpResponse {

    private final Socket socket;
    private final DataOutputStream out;
    private String charset="UTF-8";

    public HttpResponseImpl(Socket socket){
        this.socket = socket;
        
        try {
            this.out =  new DataOutputStream (socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        PrintWriter printWriter =  new PrintWriter(out,false, Charset.forName(getCharacterEncoding()));
        return printWriter;
    }

    @Override
    public void setCharacterEncoding(String charset) {
        this.charset = charset;
    }

    @Override
    public String getCharacterEncoding() {
        return charset;
    }
}
