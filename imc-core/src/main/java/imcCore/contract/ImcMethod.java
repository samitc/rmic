package imcCore.contract;

import imcCore.contract.annotations.ContractMethod;
import lombok.Data;

import java.lang.reflect.Method;

public @Data
class ImcMethod {
    private final Method method;
    private final ContractMethod methodContract;

    public ImcMethod(Method method, ContractMethod methodContract) {
        this.method = method;
        this.methodContract = methodContract;
    }

    public boolean isSendResult() {
        return methodContract.sendResult();
    }
}
