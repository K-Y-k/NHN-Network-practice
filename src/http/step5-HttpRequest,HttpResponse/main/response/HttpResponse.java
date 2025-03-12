import java.io.IOException;
import java.io.PrintWriter;

public interface HttpResponse {
    // Http Response를 Abstraction한 interface 입니다.
    
    /*
     * 클라이언트로 문자 텍스트를 전송할 수 있는 PrintWriter 객체를 반환합니다. 
     * PrintWriter는 getCharacterEncoding()에서 반환된 문자 인코딩을 사용합니다. 
     * 만약 응답의 문자 인코딩이 getCharacterEncoding에 설명된 대로 지정되지 않았다면 (즉, 이 메서드가 기본값인 ISO-8859-1을 반환하는 경우), 
     * getWriter는 이를 ISO-8859-1로 업데이트합니다.
     */
    PrintWriter getWriter() throws IOException;

    /*
     * charset – IANA 문자 집합에 의해 정의된 문자 집합만을 지정하는 문자열
     */
    void setCharacterEncoding(String charset);

    String getCharacterEncoding();

}
