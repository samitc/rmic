package imcCore.dataHandler.classHandlers;

import java.io.DataInput;
import java.io.DataOutput;

public class VoidClassHandler implements ITypeContract<Void> {
    @Override
    public int getType() {
        return 8;
    }

    @Override
    public void write(DataOutput output, Void val) {

    }

    @Override
    public Void read(DataInput input) {
        return null;
    }

    @Override
    public Void getFromObject(Object obj, long pos) {
        return null;
    }

    @Override
    public void putInObject(Object obj, long pos, Void val) {

    }
}
