package interfaces;

import util.OutputException;
import util.ParseFormException;

public abstract class FormParser {
    public final String FORM_TYPE;
    public String name;

    protected FormParser(String name, String formType) {
        this.name = name; FORM_TYPE = formType;
    }
//    public abstract void init() throws InitException;

    public abstract void parseForm() throws ParseFormException;

    /**
     * Initialize outputter that can output Forms and return it so it can be used
     * @param outputPath
     * @return FormConverter
     * @throws OutputException
     */
    public abstract FormConverter configureOutputter(String outputPath, FormConverter.Outputter type) throws OutputException;
}
