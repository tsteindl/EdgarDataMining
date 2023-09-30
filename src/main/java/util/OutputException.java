package util;

public class OutputException extends Exception {
    public Exception originalException;
    public OutputException(String message) {
        super(message);
    }
    public OutputException(Exception e) {
        super(e);
        originalException = e;
    }
}
