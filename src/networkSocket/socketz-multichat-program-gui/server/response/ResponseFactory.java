import java.util.ArrayList;
import java.util.Objects;

public class ResponseFactory {
    private static final ArrayList<Response> responseList = new ArrayList<>(){{
        // EchoResponse 객체를 등록합니다.
        add(new EchoResponse());
        
        // TimeReponse 객체를 등록합니다.
        add(new TimeResponse());
	
        // PortResponse 객체를 등록합니다.
        add(new PortResponse());

        // LoginResponse() 객체를 등록합니다.
        add(new LoginResponse());

        // BroadCastResponse() 객체를 등록합니다.
        add(new BroadCastResponse());

        // WhisperResponse() 객체를 등록합니다.
        add(new WhisperResponse());

        // BroadCastResponse() 객체를 등록합니다.
        add(new WhoamiResponse());
    }};

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
