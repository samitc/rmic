package imcCore.Utils.GeneralContractInterface;

import imcCore.contract.annotations.ContractInterface;
import imcCore.contract.annotations.ContractMethod;

@ContractInterface
public interface IContract {
    @ContractMethod
    int f(String str);

    @ContractMethod(sendResult = false)
    int f3();

    int f4();

    @ContractMethod(sendResult = true)
    void f2(int a);

    @ContractMethod
    void f1();
}
