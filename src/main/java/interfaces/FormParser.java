package interfaces;

import util.OutputException;
import util.ParseFormException;

public abstract class FormParser {
    public final String FORM_TYPE;
    public String name;

    protected FormParser(String name, String formType) {
        this.name = name; FORM_TYPE = formType;
    }

    public abstract void parseForm() throws ParseFormException;
}
