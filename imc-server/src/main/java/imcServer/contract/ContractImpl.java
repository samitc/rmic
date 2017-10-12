package imcServer.contract;

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
    private final List<Thread> contractImplRunnersThreads;

    public ContractImpl(T impl, ImcClass imcClass) throws ImplNotMaintainInterface {
        boolean isTImplImc = imcClass.getImcClass().isAssignableFrom(impl.getClass());
        if (!isTImplImc) {
            throw new ImplNotMaintainInterface(impl.getClass(), imcClass.getImcClass());
        }
        this.impl = impl;
        this.imcClass = imcClass;
        contractImplRunners = new ArrayList<>();
        contractImplRunnersThreads = new ArrayList<>();
    }

    public void open(int port) throws IOException {
        ContractImplRunners<T> contractImplRunner = new ContractImplRunners<>(impl, imcClass, port);
        Thread newRunner = new Thread(contractImplRunner);
        newRunner.start();
        contractImplRunners.add(contractImplRunner);
        contractImplRunnersThreads.add(newRunner);
    }
}
