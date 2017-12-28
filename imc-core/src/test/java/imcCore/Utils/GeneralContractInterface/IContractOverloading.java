package imcCore.Utils.GeneralContractInterface;

import imcCore.contract.annotations.ContractInterface;
import imcCore.contract.annotations.ContractMethod;

@ContractInterface
public interface IContractOverloading {
    @ContractMethod
    void f1();

    @ContractMethod
    void f2(int a, boolean b);

    @ContractMethod
    void f3(String str);

    @ContractMethod
    void f4(int a);

    @ContractMethod
    int f3(int a);

    @ContractMethod(sendResult = false)
    int f3();

    @ContractMethod
    void f4(boolean b);

    @ContractMethod(sendResult = true)
    void f2(int a);

    @ContractMethod
    String[] f5();

    @ContractMethod
    int[] f6();

    int f4();
}
