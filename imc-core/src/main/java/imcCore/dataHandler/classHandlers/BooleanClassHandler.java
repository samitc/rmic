package imcCore.dataHandler.classHandlers;

import imcCore.dataHandler.FieldHandler;
import sun.misc.Unsafe;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class BooleanClassHandler implements ITypeContract<Boolean> {
    public BooleanClassHandler() {
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public void write(DataOutput output, Boolean val) throws IOException {
        output.writeBoolean(val);
    }

    @Override
    public Boolean read(DataInput input) throws IOException {
        return input.readBoolean();
    }

    @Override
    public Boolean getFromObject(Object obj, long pos) {
        return FieldHandler.getBoolean(obj, pos);
    }

    @Override
    public void putInObject(Object obj, long pos, Boolean val) {
        FieldHandler.putBoolean(obj, pos, val);
    }

}
