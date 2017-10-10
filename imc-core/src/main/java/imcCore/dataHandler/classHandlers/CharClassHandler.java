package imcCore.dataHandler.classHandlers;

import imcCore.dataHandler.FieldHandler;
import sun.misc.Unsafe;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class CharClassHandler implements ITypeContract<Character> {
    public CharClassHandler() {
    }

    @Override
    public int getType() {
        return 1;
    }

    @Override
    public void write(DataOutput output, Character val) throws IOException {
        output.writeChar(val);
    }

    @Override
    public Character read(DataInput input) throws IOException {
        return input.readChar();
    }

    @Override
    public Character getFromObject(Object obj, long pos) {
        return FieldHandler.getChar(obj,pos);
    }

    @Override
    public void putInObject(Object obj, long pos, Character val) {
        FieldHandler.putChar(obj,pos,val);
    }
}
