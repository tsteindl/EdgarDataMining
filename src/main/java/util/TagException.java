package util;

public class TagException extends Exception {
    public TagException(String supposed, String actual) {
        super("Tag should be: " + supposed + " but is: " + actual);
    }
}
