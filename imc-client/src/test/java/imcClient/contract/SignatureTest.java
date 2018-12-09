package imcClient.contract;

import imcClient.ServerMock;
import imcClient.contract.utils.ISignatureContract;
import imcCore.contract.Exceptions.NotContractInterfaceType;
import imcCore.contract.Exceptions.NotInterfaceType;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

public class SignatureTest {
    private final static int PORT = 44444;
    private final static int VERSION = 1;
    private static ServerMock server;

    @BeforeClass
    public static void setUpServer() throws IOException, NotContractInterfaceType, NotInterfaceType {
        server = new ServerMock(PORT, VERSION, ISignatureContract.class);
        server.startServer();
    }

    @AfterClass
    public static void closeServer() {
        try {
            server.stopServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ISignatureContract createContract() {
        try {
            return ContractCaller.getInterfaceContract(ISignatureContract.class, "localhost", PORT);
        } catch (NotContractInterfaceType | NotInterfaceType | IOException notContractInterfaceType) {
            notContractInterfaceType.printStackTrace();
        }
        return null;
    }

    @Test
    public void testBoolean() {
        Assert.assertFalse(createContract().fboolean());
    }

    @Test
    public void testByte() {
        Assert.assertEquals((byte) 0, createContract().fbyte());
    }

    @Test
    public void testchar() {
        Assert.assertEquals(0, createContract().fchar());
    }

    @Test
    public void testDouble() {
        Assert.assertEquals(0, createContract().fdouble(), 0);
    }

    @Test
    public void testFloat() {
        Assert.assertEquals(0, createContract().ffloat(), 0);
    }

    @Test
    public void testInt() {
        Assert.assertEquals(0, createContract().fint());
    }

    @Test
    public void testLong() {
        Assert.assertEquals(0, createContract().flong());
    }

    @Test
    public void testShort() {
        Assert.assertEquals(0, createContract().fshort());
    }
}
