package imcCore.contract.Exceptions;

public class NotContractInterfaceType extends ContractInterfaceException {
    public NotContractInterfaceType(String className) {
        super(className);
    }
}
