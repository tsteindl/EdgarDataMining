package interfaces;

import util.InitException;
import util.OutputException;

public interface FormConverter {
    void init() throws InitException;

    void outputForm() throws OutputException;
}
