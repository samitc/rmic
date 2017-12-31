package imcServer.contract;

import imcCore.contract.Exceptions.NotContractInterfaceType;
import imcCore.contract.Exceptions.NotInterfaceType;
import imcCore.contract.ImcMethod;
import imcCore.dataHandler.MethodPocket;
import org.junit.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class ContractImplRunnersPerTest extends ContractImplRunnerTest {

    @BeforeClass
    public static void CreateRunner() throws NotContractInterfaceType, NotInterfaceType, IOException {
        init(true);
        connect(true);
    }

    MethodPocket sendRunner(MethodPocket send, int methodIndex, boolean waitForInvoke) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        ImcMethod imcMethod = imcClass.getImcMethod(methodIndex);
        byte[] buf = imcMethod.write(send);
        output.writeInt(buf.length);
        output.write(buf);
        if (!imcMethod.isSendResult()) {
            if (waitForInvoke) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        } else {
            buf = new byte[input.readInt()];
            int readed = input.read(buf);
            Assert.assertEquals(buf.length, readed);
            return imcMethod.read(buf);
        }
    }
}
