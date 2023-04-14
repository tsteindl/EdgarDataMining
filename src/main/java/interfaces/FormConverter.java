package interfaces;

import util.InitException;
import util.OutputException;

public interface FormConverter {
    void outputForm() throws OutputException;

    enum Outputter {
        CSV,
        DB
    }
}
