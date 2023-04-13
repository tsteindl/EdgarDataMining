package interfaces;

import util.OutputException;
import util.ParseFormException;

public abstract class FormParser {
    public final String FORM_TYPE;

    protected FormParser(String formType) {
        FORM_TYPE = formType;
    }
//    public abstract void init() throws InitException;

    public abstract void parseForm() throws ParseFormException;

    /**
     * Initialize outputter that can output Forms and return it so it can be used
     * @param outputPath
     * @return FormConverter
     * @throws OutputException
     */
    public abstract FormConverter output(String outputPath) throws OutputException;
}
