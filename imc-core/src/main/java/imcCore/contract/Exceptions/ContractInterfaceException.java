package imcCore.contract.Exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ContractInterfaceException extends ContractException {
    private final String className;

    public ContractInterfaceException(String className) {
        this.className = className;
    }
}
