package imcCore.dataHandler.classHandlers;

import imcCore.dataHandler.FieldHandler;
import sun.misc.Unsafe;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ShortClassHandler implements ITypeContract<Short> {
    public ShortClassHandler() {
    }

    @Override
    public int getType() {
        return 3;
    }

    @Override
    public void write(DataOutput output, Short val) throws IOException {
        output.writeShort(val);
    }

    @Override
    public Short read(DataInput input) throws IOException {
        return input.readShort();
    }

    @Override
    public Short getFromObject(Object obj, long pos) {
        return FieldHandler.getShort(obj,pos);
    }

    @Override
    public void putInObject(Object obj, long pos, Short val) {
        FieldHandler.putShort(obj,pos,val);
    }
}
