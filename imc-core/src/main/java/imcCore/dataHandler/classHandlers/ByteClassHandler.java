package imcCore.dataHandler.classHandlers;

import imcCore.dataHandler.FieldHandler;
import sun.misc.Unsafe;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ByteClassHandler implements ITypeContract<Byte> {
    public ByteClassHandler() {
    }

    @Override
    public int getType() {
        return 2;
    }

    @Override
    public void write(DataOutput output, Byte val) throws IOException {
        output.writeByte(val);
    }

    @Override
    public Byte read(DataInput input) throws IOException {
        return input.readByte();
    }

    @Override
    public Byte getFromObject(Object obj, long pos) {
        return FieldHandler.getByte(obj,pos);
    }

    @Override
    public void putInObject(Object obj, long pos, Byte val) {
        FieldHandler.putByte(obj,pos,val);
    }
}
