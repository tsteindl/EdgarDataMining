package interfaces;

import util.InitException;
import util.OutputException;

public abstract class FormParser {
    public final String FORM_TYPE;
    protected final XMLConverter outputter;

    protected FormParser(String formType, XMLConverter outputter) {
        FORM_TYPE = formType;
        this.outputter = outputter;
    }
    public abstract void init() throws InitException;

    public abstract void parseForm(String input) throws OutputException;
}
