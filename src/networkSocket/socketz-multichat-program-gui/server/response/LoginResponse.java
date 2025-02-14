import java.util.List;

public class LoginResponse implements Response {
    @Override
    public String getMethod() {
        //  method = login 설정합니다.
        return "login";
    }

    @Override
    public String execute(String value) {
        /* login list 형태로 호출하면 로그인된 전체 id를 출력 합니다.
            login list

            marco
            nhnacademy
            ann
            jone
            landy
            ...
        */
        if (value.equals("list")) {
            StringBuilder sb = new StringBuilder();

            List<String> clientIdList = MessageServer.getClientIds();
            for (String id : clientIdList) {
                sb.append("\n");
                sb.append(id);
            }

            return sb.toString();
            
            // return clientIdList.size() >0 ? String.join(System.lineSeparator(), clientIdList) : "empty";
        }

        

        /* MessageServer.addClient()를 이용해서 clientMap에 client Socket을 추가합니다.
         * client Socket은 Session을 이용해서 획득할 수 있습니다.
         */
        boolean loginSuccess = MessageServer.addClient(value, Session.getCurrentSocket());

        /* loginSuccess == true 이면 Session.initializeId를 호출해서 현제 clinet의 id를 설정합니다.
             - login success!를 client에게 반환합니다.
        */
        if (loginSuccess) {
            Session.initializeId(value);
            return "login success!";
        }
        

        // loginSuccess == false이면 "login fail!" 반환합니다.
        return "login fail!";
    }
}
