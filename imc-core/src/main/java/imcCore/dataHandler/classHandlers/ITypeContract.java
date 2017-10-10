package imcCore.dataHandler.classHandlers;

import java.io.DataOutput;
import java.io.IOException;

public interface ITypeContract<T> {
    int getType();

    void writeToStream(Object obj,long pos, DataOutput output) throws IOException;
}
