package imcCore.dataHandler.classHandlers;

import imcCore.dataHandler.FieldHandler;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class FloatClassHandler implements ITypeContract<Float> {
    public FloatClassHandler() {
    }

    @Override
    public int getType() {
        return 6;
    }

    @Override
    public void write(DataOutput output, Float val) throws IOException {
        output.writeFloat(val);
    }

    @Override
    public Float read(DataInput input) throws IOException {
        return input.readFloat();
    }

    @Override
    public Float getFromObject(Object obj, long pos) {
        return FieldHandler.getFloat(obj, pos);
    }

    @Override
    public void putInObject(Object obj, long pos, Float val) {
        FieldHandler.putFloat(obj,pos,val);
    }
}
