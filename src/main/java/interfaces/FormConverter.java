package interfaces;

import util.InitException;
import util.OutputException;

public interface FormConverter { //TODO: overthink if we really need this class
    void outputForm(String outputPath) throws OutputException;

}
