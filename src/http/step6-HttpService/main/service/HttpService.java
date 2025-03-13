/* 
 * HttpService는 Http Request, Http Response를 parameter를 인자로 받고 
 * http 요청을 처리하고 응답하는 역할을 합니다.
    - HttpService interface 이해합니다.
 */
public interface HttpService {

    default void service(HttpRequest httpRequest, HttpResponse httpResponse){
        // httpRequest.getMethod()에 따라서 doGet() or doPost()를 호출합니다.
        if (httpRequest.getMethod().equals("GET")) {
            doGet(httpRequest, httpResponse);
        } else if(httpRequest.getMethod().equals("POST")) {
            doPost(httpRequest, httpResponse);
        }
    }

    default void doGet(HttpRequest httpRequest, HttpResponse httpResponse){
        throw new RuntimeException("405 - Method Not Allowd");
    }

    default void doPost(HttpRequest httpRequest, HttpResponse httpResponse){
        throw new RuntimeException("405 - Method Not Allowd");
    }
}