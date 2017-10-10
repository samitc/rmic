package imcCore.dataHandler;

import imcCore.contract.ImcClass;
import lombok.Data;
import lombok.val;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;

@Data
class ImcMethodDesc implements ImcData {
    private final int methodCode;
    private final ImcClassDesc[] params;
    private final ImcClassDesc retData;

    ImcMethodDesc(Method method, ImcClass imcClass) {
        Method[] methods = imcClass.getContractMethods().toArray(Method[]::new);
        int metPos = -1;
        for (int i = 0; metPos == -1 && i < methods.length; i++) {
            if (method == methods[i]) {
                metPos = i;
            }
        }
        methodCode = metPos;
        retData = imcClass.getImcMethod(metPos).isSendResult() ? new ImcClassDesc(method.getReturnType()) : null;
        Class<?>[] parameters = method.getParameterTypes();
        params = parameters.length == 0 ? null : Arrays.stream(parameters).map(ImcClassDesc::new).toArray(ImcClassDesc[]::new);
    }
    public byte[] toBytes(Object retObj,Object ...paramsObj) throws IOException {
        val buf=new ByteArrayOutputStream(){
            @Override
            public synchronized byte[] toByteArray() {
                return buf;
            }
        };
        val dataOutput=new DataOutputStream(buf);
        dataOutput.writeInt(methodCode);
        if (retData!=null){
            retData.writeBytes(retObj,dataOutput);
        }
        if (params!=null){
            for (int i = 0; i < params.length; i++) {
                params[i].writeBytes(paramsObj[i],dataOutput);
            }
        }
        return buf.toByteArray();
    }
}
