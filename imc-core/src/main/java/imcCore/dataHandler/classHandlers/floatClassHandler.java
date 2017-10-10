package imcCore.dataHandler.classHandlers;

import imcCore.dataHandler.FieldHandler;

import java.io.DataOutput;
import java.io.IOException;

public class floatClassHandler implements ITypeContract<Float> {
    public floatClassHandler() {
    }

    @Override
    public int getType() {
        return 6;
    }

    @Override
    public void writeToStream(Object obj, long pos, DataOutput output) throws IOException {
        output.writeFloat(FieldHandler.getFloat(obj, pos));
    }
}
