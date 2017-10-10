package imcCore.dataHandler.classHandlers;

import imcCore.dataHandler.FieldHandler;
import sun.misc.Unsafe;

import java.io.DataOutput;
import java.io.IOException;

public class byteClassHandler implements ITypeContract<Byte> {
    public byteClassHandler() {
    }

    @Override
    public int getType() {
        return 2;
    }

    @Override
    public void writeToStream(Object obj, long pos, DataOutput output) throws IOException {
        output.writeByte(FieldHandler.getByte(obj,pos));
    }
}
