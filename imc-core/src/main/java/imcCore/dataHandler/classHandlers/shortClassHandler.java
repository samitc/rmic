package imcCore.dataHandler.classHandlers;

import imcCore.dataHandler.FieldHandler;
import sun.misc.Unsafe;

import java.io.DataOutput;
import java.io.IOException;

public class shortClassHandler implements ITypeContract<Short> {
    public shortClassHandler() {
    }

    @Override
    public int getType() {
        return 3;
    }

    @Override
    public void writeToStream(Object obj, long pos, DataOutput output) throws IOException {
output.writeShort(FieldHandler.getShort(obj,pos));
    }
}
