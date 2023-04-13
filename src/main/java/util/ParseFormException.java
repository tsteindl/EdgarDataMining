package util;

import org.w3c.dom.Node;

public class ParseFormException extends Exception {
    public ParseFormException(Node n) {
        super("Node should be null but is " + n);
    }
}
