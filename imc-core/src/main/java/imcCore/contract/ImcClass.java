package imcCore.contract;

import imcCore.contract.Exceptions.NotContractInterfaceType;
import imcCore.contract.Exceptions.NotInterfaceType;
import imcCore.contract.annotations.ContractInterface;
import imcCore.contract.annotations.ContractMethod;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public @Data
class ImcClass {
    private final Class<?> imcClass;
    private final ContractInterface interfaceContract;
    private @Getter(AccessLevel.PACKAGE)
    final List<ImcMethod> contractMethod;

    public ImcClass(Class<?> imcClass) throws NotInterfaceType, NotContractInterfaceType {
        if (!imcClass.isInterface()) {
            throw new NotInterfaceType(imcClass.getSimpleName());
        }
        ContractInterface interfaceContract = imcClass.getDeclaredAnnotation(ContractInterface.class);
        if (interfaceContract == null) {
            throw new NotContractInterfaceType(imcClass.getSimpleName());
        }
        this.imcClass = imcClass;
        this.interfaceContract = interfaceContract;
        contractMethod = new ArrayList<>();
        for (Method method :
                imcClass.getMethods()) {
            ContractMethod methodContract = method.getDeclaredAnnotation(ContractMethod.class);
            if (methodContract != null) {
                contractMethod.add(new ImcMethod(method, methodContract));
            }
        }
        contractMethod.sort((o1, o2) -> {
            Method m1 = o1.getMethod();
            Method m2 = o2.getMethod();
            int curCom = m1.getName().compareTo(m2.getName());
            if (curCom != 0) {
                return curCom;
            }
            int parameterCount = m1.getParameterCount();
            curCom = parameterCount - m2.getParameterCount();
            if (curCom != 0) {
                return curCom;
            }
            Class<?>[] m1Parameters = m1.getParameterTypes();
            Class<?>[] m2Parameters = m2.getParameterTypes();
            for (int i = 0; i < parameterCount; i++) {
                curCom = m1Parameters[i].getName().compareTo(m2Parameters[i].getName());
                if (curCom != 0) {
                    return curCom;
                }
            }
            return 0;
        });
        for (int i = 0; i < contractMethod.size(); i++) {
            contractMethod.get(i).setMethodIndex(i);
        }
    }

    public Stream<Method> getContractMethods() {
        return contractMethod.stream().map(ImcMethod::getMethod);
    }

    public ImcMethod getImcMethod(int index) {
        return contractMethod.get(index);
    }

    public boolean isSendContract() {
        return interfaceContract.sendContract();
    }
}
