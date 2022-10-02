package interfaces;

import util.InitException;
import util.OutputException;

public abstract class FormParser {
    public final String FORM_TYPE;
    protected final XMLConverter model;

    protected FormParser(String formType, XMLConverter model) {
        FORM_TYPE = formType;
        this.model = model;
    }
    public abstract void init() throws InitException;

    public abstract void parseForm(String input) throws OutputException;
}
