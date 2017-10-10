package imcCore.dataHandler.classHandlers;

import imcCore.dataHandler.FieldHandler;
import sun.misc.Unsafe;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;

public class DoubleClassHandler implements ITypeContract<Double> {

    public DoubleClassHandler() {
    }

    @Override
    public int getType() {
        return 7;
    }

    @Override
    public void write(DataOutput output, Double val) throws IOException {
        output.writeDouble(val);
    }

    @Override
    public Double read(DataInput input) throws IOException {
        return input.readDouble();
    }

    @Override
    public Double getFromObject(Object obj, long pos) {
        return FieldHandler.getDouble(obj,pos);
    }

    @Override
    public void putInObject(Object obj, long pos, Double val) {
        FieldHandler.putDouble(obj,pos,val);
    }

}
