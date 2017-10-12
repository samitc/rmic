package imcCore.dataHandler;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
@Builder
public class MethodPocket {
    private final Object retObj;
    @Singular("addParam")
    private final List<Object> paramsObject;

    public MethodPocket(Object retObj, List<Object> paramsObject) {
        this.retObj = retObj;
        this.paramsObject = paramsObject;
    }

    boolean hasParams() {
        return paramsObject != null && paramsObject.size() > 0;
    }

    boolean hasRetObj() {
        return retObj != null;
    }
}
