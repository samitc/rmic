package imcServer.contract;

import imcCore.contract.Exceptions.NotContractInterfaceType;
import imcCore.contract.Exceptions.NotInterfaceType;
import imcCore.contract.ImcClass;
import imcServer.contract.Exception.ImplNotMaintainInterface;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Data
public class ContractImpl<T> {
    @Getter(AccessLevel.PACKAGE)
    private static final int version;

    static {
        version = 1;
    }

    private final T impl;
    private final ImcClass imcClass;
    private final List<ContractImplRunners<T>> contractImplRunners;

    public ContractImpl(T impl, Class<?> classType) throws ImplNotMaintainInterface, NotContractInterfaceType, NotInterfaceType {
        imcClass=new ImcClass(classType);
        boolean isTImplImc = imcClass.getImcClass().isAssignableFrom(impl.getClass());
        if (!isTImplImc) {
            throw new ImplNotMaintainInterface(impl.getClass(), imcClass.getImcClass());
        }
        this.impl = impl;
        contractImplRunners = new ArrayList<>();
    }

    public void open(int port) throws IOException {
        ContractImplRunners<T> contractImplRunner = ContractImplRunners.createContractImplRunners(impl, imcClass, port);
        contractImplRunner.startServerAsync();
        contractImplRunners.add(contractImplRunner);
    }

    @Deprecated
    public void openNonPersistent(int port) throws IOException {
        ContractImplRunners<T> contractImplRunner = ContractImplRunners.createNonPerContractImplRunners(impl, imcClass, port);
        contractImplRunner.startServerAsync();
        contractImplRunners.add(contractImplRunner);
    }

    public void close() {
        contractImplRunners.forEach(ContractImplRunners::stopRunner);
    }
}
