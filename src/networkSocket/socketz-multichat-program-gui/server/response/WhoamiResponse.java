public class WhoamiResponse implements Response {
    @Override
    public String getMethod() {
        // method = "whoami" 설정 합니다.
        return "whoami";
    }

    @Override
    public String execute(String value) {
        // 로그인되어 있지 않다면 "login required!"  반환합니다.
        if (!Session.isLogin()) {
            return "login required!";
        }

        // 로그인 되어 있다면 my id is [marco] 형식으로 응답합니다.
        return String.format("my id is [%s]", Session.getCurrentId());
    }
}
