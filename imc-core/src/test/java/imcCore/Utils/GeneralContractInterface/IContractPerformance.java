package imcCore.Utils.GeneralContractInterface;

import imcCore.Utils.GeneralClasses.E;
import imcCore.contract.annotations.ContractInterface;
import imcCore.contract.annotations.ContractMethod;

import java.util.List;

@ContractInterface
public interface IContractPerformance {

    @ContractMethod
    void f1();

    @ContractMethod
    int f2(List<Integer> values);

    @ContractMethod(sendResult = false)
    float f3(List<Float> values);

    @ContractMethod
    List<Integer> f4(int... vals);

    @ContractMethod
    String f5(char[] chars,char[] oChars);

    @ContractMethod(sendResult = false)
    Integer f6(int val,int val2);

    @ContractMethod()
    E f7(E val);
}
