package imcCore.contract;

import imcCore.contract.annotations.ContractMethod;
import imcCore.dataHandler.ImcMethodDesc;
import imcCore.dataHandler.MethodPocket;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public @Data
class ImcMethod {
    private final Method method;
    private final ContractMethod methodContract;
    @Setter(AccessLevel.PACKAGE)
    @Getter(AccessLevel.PRIVATE)
    private int methodIndex;
    @Setter(AccessLevel.PRIVATE)
    private ImcMethodDesc imcMethodDesc;

    public ImcMethod(Method method, ContractMethod methodContract) {
        this.method = method;
        this.methodContract = methodContract;
        setMethodIndex(-1);
    }

    public boolean isSendResult() {
        return methodContract.sendResult();
    }

    public MethodPocket read(byte[] buf) throws IllegalAccessException, IOException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        return getImcMethodDesc().fromBytes(buf);
    }

    private ImcMethodDesc getImcMethodDesc() {
        if (imcMethodDesc == null) {
            imcMethodDesc = new ImcMethodDesc(this, methodIndex);
        }
        return imcMethodDesc;
    }

    public byte[] write(MethodPocket data) throws IOException {
        return getImcMethodDesc().toBytes(data);
    }
}
