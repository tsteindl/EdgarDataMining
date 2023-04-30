package util;

import org.w3c.dom.Node;

public class ParseFormException extends Exception {
    public ParseFormException(String formName, String msg) {
        super("Form " + formName + ": " + msg);
    }
    public ParseFormException(String formName, Node n) {
        super("Form " + formName + ": Node should be null but is " + n);
    }
}
