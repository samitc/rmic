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

    static class DataInputLenStream implements DataInputLen {
        private final byte[] buf;
        private int pos = 0;
        private final byte[] readBuffer = new byte[8];

        DataInputLenStream(byte[] buf) {
            this.buf = buf;
        }

        @Override
        public int readed() {
            return pos;
        }

        @Override
        public void readFully(byte[] b) throws IOException {
            this.readFully(b, 0, b.length);
        }

        @Override
        public void readFully(byte[] b, int off, int len) throws IOException {
            int readLen = Math.min(len, buf.length - pos);
            System.arraycopy(buf, pos, b, off, readLen);
            pos += readLen;
            if (readLen < len) {
                throw new EOFException();
            }
        }

        @Override
        public int skipBytes(int n) throws IOException {
            int skipLen = Math.min(n, buf.length - pos);
            pos += skipLen;
            return skipLen;
        }

        @Override
        public boolean readBoolean() throws IOException {
            return readByte() != 0;
        }

        private int read() throws IOException {
            if (pos >= buf.length) {
                throw new EOFException();
            }
            return buf[pos++] & 0xff;
        }

        @Override
        public byte readByte() throws IOException {
            if (pos >= buf.length) {
                throw new EOFException();
            }
            return buf[pos++];
        }

        @Override
        public int readUnsignedByte() throws IOException {
            int val = readByte();
            return val & 0xff;
        }

        @Override
        public short readShort() throws IOException {
            return (short) readUnsignedShort();
        }

        @Override
        public int readUnsignedShort() throws IOException {
            return (((read()) << 8) + (read()));
        }

        @Override
        public char readChar() throws IOException {
            return (char) ((read() << 8) + (read()));
        }

        @Override
        public int readInt() throws IOException {
            return ((read() << 24) + (read() << 16) + (read() << 8) + (read()));
        }

        @Override
        public long readLong() throws IOException {
            readFully(readBuffer, 0, 8);
            return (((long)readBuffer[0] << 56) +
                    ((long)(readBuffer[1] & 255) << 48) +
                    ((long)(readBuffer[2] & 255) << 40) +
                    ((long)(readBuffer[3] & 255) << 32) +
                    ((long)(readBuffer[4] & 255) << 24) +
                    ((readBuffer[5] & 255) << 16) +
                    ((readBuffer[6] & 255) <<  8) +
                    ((readBuffer[7] & 255) <<  0));
        }

        @Override
        public float readFloat() throws IOException {
            return Float.intBitsToFloat(readInt());
        }

        @Override
        public double readDouble() throws IOException {
            return Double.longBitsToDouble(readLong());
        }

        @Override
        public String readLine() throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public String readUTF() throws IOException {
            throw new UnsupportedOperationException();
        }
    }

}
