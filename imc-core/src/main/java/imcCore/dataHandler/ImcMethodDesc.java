package imcCore.dataHandler;

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

    private ImcMethodDesc(ImcClass imcClass, int methodCode, Method method) {
        this(imcClass.getImcMethod(methodCode), methodCode, method);
    }

    public ImcMethodDesc(ImcMethod imcMethod, int methodCode) {
        this(imcMethod, methodCode, imcMethod.getMethod());
    }

    ImcMethodDesc(ImcClass imcClass, int methodIndex) {
        this(imcClass.getImcMethod(methodIndex), methodIndex);
    }

    ImcMethodDesc(Method method, ImcClass imcClass) {

        this(imcClass, getMethodPos(method, imcClass), method);
    }

    private static int getMethodPos(Method method, ImcClass imcClass) {
        return imcClass.getMethodIndex(method);
    }

    private static void writeObject(DataOutput output, ImcClassDesc classDesc, Object obj) throws IOException {
        classDesc.writeImcClassDescBytes(obj, output);
    }

    private Object readObject(DataInput input, ImcClassDesc classDesc) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
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
        val dataInput = new DataInputStream(new ByteArrayInputStream(bytes));
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
}
