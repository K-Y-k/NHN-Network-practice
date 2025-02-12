import java.util.ArrayList;
import java.util.Objects;

public class ResponseFactory {
    private static final ArrayList<Response> responseList = new ArrayList<>(){{
        // EchoResponse 객체를 등록 합니다.
        add(new EchoResponse());
    }};


    /* responseList에서 parameter로 전달된 method에 해당된 구현체를 반환합니다.
       response가 존재하지 않다면 ResponseNotFoundException을 발생합니다.
     */
    public static Response getResponse(String method){
        Response response = responseList.stream()
                .filter(o->o.validate(method))
                .findFirst()
                .orElse(null);

        if(Objects.isNull(response)){
            throw new ResponseNotFoundException();
        }
        
        return response;
    }
}
 