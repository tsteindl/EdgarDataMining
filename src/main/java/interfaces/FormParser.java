package interfaces;

import util.InitException;
import util.OutputException;
import util.ParseFormException;

public abstract class FormParser {
    public final String FORM_TYPE;

    protected FormParser(String formType) {
        FORM_TYPE = formType;
    }
//    public abstract void init() throws InitException;

    public abstract void parseForm() throws ParseFormException;

    public abstract XMLConverter output(String outputPath) throws OutputException;
}
