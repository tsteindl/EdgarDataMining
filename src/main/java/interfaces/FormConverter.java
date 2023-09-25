package interfaces;

import util.InitException;
import util.OutputException;

public interface FormConverter { //TODO: overthink if we really need this class
    void outputForm() throws OutputException;

    enum Outputter {
        CSV,
        DB
    }
}
