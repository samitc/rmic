package imcCore.Utils.GeneralContractInterface;

import imcCore.contract.annotations.ContractInterface;

@ContractInterface(sendContract = true)
public interface IEmptyContract {
    void f1();

    void f2(int a);

    int f3();

    int f4();
}
