package imcCore.dataHandler.classHandlers;

import imcCore.dataHandler.FieldHandler;
import sun.misc.Unsafe;

import java.io.DataOutput;
import java.io.IOException;

public class longClassHandler implements ITypeContract<Long> {
    public longClassHandler() {
    }

    @Override
    public int getType() {
        return 5;
    }

    @Override
    public void writeToStream(Object obj, long pos, DataOutput output) throws IOException {
        output.writeLong(FieldHandler.getLong(obj, pos));
    }
}
