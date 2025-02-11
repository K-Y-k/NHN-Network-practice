public class NotLoggedInException extends RuntimeException {
    public NotLoggedInException() {
        super();
    }

    public NotLoggedInException(String message) {
        super(message);
    }

}
