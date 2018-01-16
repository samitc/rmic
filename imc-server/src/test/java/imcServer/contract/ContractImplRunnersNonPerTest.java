package imcServer.contract;

import imcCore.contract.Exceptions.NotContractInterfaceType;
import imcCore.contract.Exceptions.NotContractMethodException;
import imcCore.contract.Exceptions.NotInterfaceType;
import imcCore.contract.ImcMethod;
import imcCore.dataHandler.MethodPocket;
import org.junit.Assert;
import org.junit.BeforeClass;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class ContractImplRunnersNonPerTest extends ContractImplRunnerTest {

    @BeforeClass
    public static void createRunner() throws NotContractInterfaceType, NotInterfaceType, IOException {
        ContractImplRunnerTest.init(false);
    }

    private void serverDataFirstConnect() throws IOException {
        connect(false);
        input.close();
        output.close();
        client.close();
    }

    MethodPocket sendRunner(MethodPocket send, int methodIndex, boolean waitForInvoke) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        serverDataFirstConnect();
        connect(false);
        DataInputStream input = new DataInputStream(client.getInputStream());
        DataOutputStream output = new DataOutputStream(client.getOutputStream());
        ImcMethod imcMethod;
        try {
            imcMethod = imcClass.getImcMethod(methodIndex);
        } catch (NotContractMethodException e) {
            return null;
        }
        byte[] buf = imcMethod.write(send);
        output.writeInt(buf.length);
        output.write(buf);
        if (!imcMethod.isSendResult()) {
            boolean exHappen = false;
            try {
                Assert.assertEquals(-1, input.readInt());
            } catch (EOFException ex) {
                exHappen = true;
            }
            Assert.assertTrue(exHappen);
            client.close();
            return null;
        } else {
            buf = new byte[input.readInt()];
            int readed = input.read(buf);
            Assert.assertEquals(buf.length, readed);
            client.close();
            return imcMethod.read(buf);
        }
    }
}
