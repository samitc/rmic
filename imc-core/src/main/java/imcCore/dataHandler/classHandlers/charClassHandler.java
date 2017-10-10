package imcCore.dataHandler.classHandlers;

import imcCore.dataHandler.FieldHandler;
import sun.misc.Unsafe;

import java.io.DataOutput;
import java.io.IOException;

public class charClassHandler implements ITypeContract<Character> {
    public charClassHandler() {
    }

    @Override
    public int getType() {
        return 1;
    }

    @Override
    public void writeToStream(Object obj, long pos, DataOutput output) throws IOException {
        output.writeChar(FieldHandler.getChar(obj,pos));
    }
}
