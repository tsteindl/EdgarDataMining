package interfaces;

import util.InitException;
import util.OutputException;

public interface FormConverter {
    void outputForm(String outputPath) throws OutputException;
}
