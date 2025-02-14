import org.apache.commons.lang3.StringUtils;

import java.net.Socket;
import java.util.Objects;

public class Session {
    // current thread에서 사용하고 있는 client Socket을 저장하기 위해서 currentSocketLocal 변수를 초기화 합니다.
    private static final ThreadLocal<Socket> currentSocketLocal = new ThreadLocal<>();

    // current thread의 client id를 저장하기 위해서 currentIdLocal 변수를 초기화 합니다.
    private static final ThreadLocal<String> currentIdLocal = new ThreadLocal<>();

    public static void initializeSocket(Socket socket){
        // current thread 에 연결되어 있는 client Socket을 currentSocketLocal 변수에 설정합니다.
        currentSocketLocal.set(socket);
    }

    public static void initializeId(String id){
        // current thread에 연결되어 있는 client id를 currentIdLocal 변수에 설정합니다.
        currentIdLocal.set(id);
    }

    public static void reset(){
        // currentIdLocal, currentSocketLocal 초기화 합니다.
        currentIdLocal.remove();
        currentSocketLocal.remove();
    }

    public static Socket getCurrentSocket(){
        // current thread에 연결되어 있는 cleint Socket을 반환합니다.
        return currentSocketLocal.get();
    }

    public static String getCurrentId(){
        // current thread에 연결되어 있는 cleint id를 반환합니다.
        return currentIdLocal.get();
    }

    public static boolean isLogin(){
        // 로그인 여부를 반환합니다.
        if (currentIdLocal == null) {
            return false;
        }
        
        return true;
    }
}
