package interfaces;

import util.InitException;
import util.OutputException;

public interface XMLConverter {
    void init() throws InitException;

    void output() throws OutputException;
}
