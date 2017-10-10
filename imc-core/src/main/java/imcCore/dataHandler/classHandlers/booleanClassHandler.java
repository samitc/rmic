package imcCore.dataHandler.classHandlers;

import imcCore.dataHandler.FieldHandler;
import sun.misc.Unsafe;

import java.io.DataOutput;
import java.io.IOException;

public class booleanClassHandler implements ITypeContract<Boolean> {
    public booleanClassHandler() {
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public void writeToStream(Object obj, long pos, DataOutput output) throws IOException {
        output.writeBoolean(FieldHandler.getBoolean(obj,pos));
    }
}
