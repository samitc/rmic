package imcCore.dataHandler.classHandlers;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface ITypeContract<T> {
    int getType();

    void write(DataOutput output, T val) throws IOException;

    T read(DataInput input) throws IOException;

    T getFromObject(Object obj, long pos);

    void putInObject(Object obj, long pos, T val);

    @SuppressWarnings("unchecked")
    default void writeO(DataOutput output, Object val) throws IOException {
        write(output, (T) val);
    }

    default void writeToStream(Object obj, long pos, DataOutput output) throws IOException {
        write(output, getFromObject(obj, pos));
    }

    default void readFromStream(Object obj, int pos, DataInput input) throws IOException {
        putInObject(obj, pos, read(input));
    }
}
