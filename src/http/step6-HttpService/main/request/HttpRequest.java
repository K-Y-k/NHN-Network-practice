import java.util.Map;

public interface HttpRequest {
    // GET, POST, ....
    String getMethod();

    // ?page=1&sort=age, ex) getParameter("sort") , return age
    String getParameter(String name);

    // paramter를 map 형태로 반환합니다.
    Map<String, String> getParameterMap();

    // header의 value를 반환합니다.
    String getHeader(String name);

    // request에 값을(name->value) 설정합니다.
    void setAttribute(String name, Object o);

    // request에 설정된 값을 반환합니다.
    Object getAttribute(String name);

    // 요청 경로를 반환합니다. GET /index.html?page=1 -> /index.html
    String getRequestURI();
}
