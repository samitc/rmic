package imcCore.dataHandler.classHandlers;

import imcCore.dataHandler.FieldHandler;
import sun.misc.Unsafe;

import java.io.DataOutput;
import java.io.IOException;

public class intClassHandler implements ITypeContract<Integer> {
    public intClassHandler() {
    }

    @Override
    public int getType() {
        return 4;
    }

    @Override
    public void writeToStream(Object obj, long pos, DataOutput output) throws IOException {
        output.writeInt(FieldHandler.getInt(obj, pos));
    }
}
