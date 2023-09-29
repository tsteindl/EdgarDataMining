package util;

public class OutputException extends Exception{
    public OutputException(String message) {
        super(message);
    }
    public OutputException(Exception message) {
        super(message);
    }

}
