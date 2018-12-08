package imcClient.contract.utils;

import imcCore.contract.annotations.ContractInterface;
import imcCore.contract.annotations.ContractMethod;

@ContractInterface
public interface ISignatureContract {
    @ContractMethod(sendResult = false)
    int fint();
    @ContractMethod(sendResult = false)
    float ffloat();
    @ContractMethod(sendResult = false)
    double fdouble();
    @ContractMethod(sendResult = false)
    boolean fboolean();
    @ContractMethod(sendResult = false)
    byte fbyte();
    @ContractMethod(sendResult = false)
    short fshort();
    @ContractMethod(sendResult = false)
    long flong();
    @ContractMethod(sendResult = false)
    char fchar();
}
