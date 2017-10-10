package imcCore.dataHandler.classHandlers;

import imcCore.dataHandler.FieldHandler;
import sun.misc.Unsafe;

import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;

public class doubleClassHandler implements ITypeContract<Double> {

    public doubleClassHandler() {
    }

    @Override
    public int getType() {
        return 7;
    }

    @Override
    public void writeToStream(Object obj, long pos, DataOutput output) throws IOException {
        output.writeDouble(FieldHandler.getDouble(obj,pos));
    }

}
