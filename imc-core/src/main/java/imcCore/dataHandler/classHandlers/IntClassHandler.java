package imcCore.dataHandler.classHandlers;

import imcCore.dataHandler.FieldHandler;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class IntClassHandler implements ITypeContract<Integer> {
    public IntClassHandler() {
    }

    @Override
    public int getType() {
        return 4;
    }

    @Override
    public void write(DataOutput output, Integer val) throws IOException {
        output.writeInt(val);
    }

    @Override
    public Integer read(DataInput input) throws IOException {
        return input.readInt();
    }

    @Override
    public Integer getFromObject(Object obj, long pos) {
        return FieldHandler.getInt(obj, pos);
    }

    @Override
    public void putInObject(Object obj, long pos, Integer val) {
        FieldHandler.putInt(obj,pos,val);
    }
}
