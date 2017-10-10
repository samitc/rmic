package imcCore.dataHandler.classHandlers;

import imcCore.dataHandler.FieldHandler;
import sun.misc.Unsafe;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class LongClassHandler implements ITypeContract<Long> {
    public LongClassHandler() {
    }

    @Override
    public int getType() {
        return 5;
    }

    @Override
    public void write(DataOutput output, Long val) throws IOException {
        output.writeLong(val);
    }

    @Override
    public Long read(DataInput input) throws IOException {
        return input.readLong();
    }

    @Override
    public Long getFromObject(Object obj, long pos) {
        return FieldHandler.getLong(obj, pos);
    }

    @Override
    public void putInObject(Object obj, long pos, Long val) {
        FieldHandler.putLong(obj,pos,val);
    }
}
