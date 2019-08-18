package imcCore.dataHandler;

import imcCore.contract.Exceptions.NotContractMethodException;
import imcCore.contract.ImcClass;
import imcCore.contract.ImcMethod;
import lombok.Data;
import lombok.val;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

@Data
public class ImcMethodDesc {
    private final int methodCode;
    private final ImcClassDesc[] params;
    private final ImcClassDesc retData;

    private ImcMethodDesc(ImcMethod imcMethod, int methodCode, Method method) {
        this.methodCode = methodCode;
        retData = imcMethod.isSendResult() ? ImcClassDesc.getImcClassDesc(method.getReturnType()) : null;
        Class<?>[] parameters = method.getParameterTypes();
        params = parameters.length == 0 ? null : Arrays.stream(parameters).map(ImcClassDesc::getImcClassDesc).toArray(ImcClassDesc[]::new);
    }

    private ImcMethodDesc(ImcClass imcClass, int methodCode, Method method) throws NotContractMethodException {
        this(imcClass.getImcMethod(methodCode), methodCode, method);
    }

    public ImcMethodDesc(ImcMethod imcMethod, int methodCode) {
        this(imcMethod, methodCode, imcMethod.getMethod());
    }

    ImcMethodDesc(ImcClass imcClass, int methodIndex) throws NotContractMethodException {
        this(imcClass.getImcMethod(methodIndex), methodIndex);
    }

    ImcMethodDesc(Method method, ImcClass imcClass) throws NotContractMethodException {
        this(imcClass, getMethodPos(method, imcClass), method);
    }

    private static int getMethodPos(Method method, ImcClass imcClass) {
        return imcClass.getMethodIndex(method);
    }

    private static void writeObject(DataOutputStream output, ImcClassDesc classDesc, Object obj) throws IOException {
        classDesc.writeImcClassDescBytes(obj, new DataOutputLenStream(output));
    }

    private Object readObject(DataInputLen input, ImcClassDesc classDesc) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        return classDesc.readImcClassDescBytes(input);
    }

    public byte[] toBytes(MethodPocket methodPocket) throws IOException {
        val buf = new ByteArrayOutputStream();
        val dataOutput = new DataOutputStream(buf);
        dataOutput.writeInt(methodCode);
        dataOutput.writeByte(ImcMethodDescFlags.readFlags(methodPocket));
        if (retData != null && methodPocket.hasRetObj()) {
            writeObject(dataOutput, retData, methodPocket.getRetObj());
        }
        if (params != null && methodPocket.hasParams()) {
            for (int i = 0; i < params.length; i++) {
                writeObject(dataOutput, params[i], methodPocket.getParamsObject().get(i));
            }
        }
        return buf.toByteArray();
    }

    public MethodPocket fromBytes(byte[] bytes) throws IOException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        val dataInput = new DataInputLenStream(bytes);
        val methodPocket = MethodPocket.builder();
        int methodCode = dataInput.readInt();
        byte flag = dataInput.readByte();
        if (this.methodCode != methodCode) {
            return null;
        }
        if (retData != null && ImcMethodDescFlags.hasSendRetObj(flag)) {
            methodPocket.retObj(readObject(dataInput, retData));
        }
        if (params != null && ImcMethodDescFlags.hasSendParams(flag)) {
            for (ImcClassDesc param : params) {
                methodPocket.addParam(readObject(dataInput, param));
            }
        }
        return methodPocket.build();
    }

    static class DataOutputLenStream implements DataOutputLen {
        public final DataOutputStream stream;

        DataOutputLenStream(DataOutputStream stream) {
            this.stream = stream;
        }

        @Override
        public int size() {
            return stream.size();
        }

        @Override
        public void write(int b) throws IOException {
            stream.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            stream.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            stream.write(b, off, len);
        }

        @Override
        public void writeBoolean(boolean v) throws IOException {
            stream.writeBoolean(v);
        }

        @Override
        public void writeByte(int v) throws IOException {
            stream.writeByte(v);
        }

        @Override
        public void writeShort(int v) throws IOException {
            stream.writeShort(v);
        }

        @Override
        public void writeChar(int v) throws IOException {
            stream.writeChar(v);
        }

        @Override
        public void writeInt(int v) throws IOException {
            stream.writeInt(v);
        }

        @Override
        public void writeLong(long v) throws IOException {
            stream.writeLong(v);
        }

        @Override
        public void writeFloat(float v) throws IOException {
            stream.writeFloat(v);
        }

        @Override
        public void writeDouble(double v) throws IOException {
            stream.writeDouble(v);
        }

        @Override
        public void writeBytes(String s) throws IOException {
            stream.writeBytes(s);
        }

        @Override
        public void writeChars(String s) throws IOException {
            stream.writeChars(s);
        }

        @Override
        public void writeUTF(String s) throws IOException {
            stream.writeUTF(s);
        }
    }

    static class DataInputLenStream extends ByteArrayInputStream implements DataInputLen {
        private final DataInputStream s;

        DataInputLenStream(byte[] buf) {
            super(buf);
            s = new DataInputStream(this);
        }

        @Override
        public int readed() {
            return pos;
        }

        @Override
        public void readFully(byte[] b) throws IOException {
            s.readFully(b);
        }

        @Override
        public void readFully(byte[] b, int off, int len) throws IOException {
            s.readFully(b, off, len);
        }

        @Override
        public int skipBytes(int n) throws IOException {
            return s.skipBytes(n);
        }

        @Override
        public boolean readBoolean() throws IOException {
            return s.readBoolean();
        }

        @Override
        public byte readByte() throws IOException {
            return s.readByte();
        }

        @Override
        public int readUnsignedByte() throws IOException {
            return s.readUnsignedByte();
        }

        @Override
        public short readShort() throws IOException {
            return s.readShort();
        }

        @Override
        public int readUnsignedShort() throws IOException {
            return s.readUnsignedShort();
        }

        @Override
        public char readChar() throws IOException {
            return s.readChar();
        }

        @Override
        public int readInt() throws IOException {
            return s.readInt();
        }

        @Override
        public long readLong() throws IOException {
            return s.readLong();
        }

        @Override
        public float readFloat() throws IOException {
            return s.readFloat();
        }

        @Override
        public double readDouble() throws IOException {
            return s.readDouble();
        }

        @Override
        public String readLine() throws IOException {
            return s.readLine();
        }

        @Override
        public String readUTF() throws IOException {
            return s.readUTF();
        }
    }
}
