package imcClient.contract;

import imcCore.contract.Exceptions.NotContractInterfaceType;
import imcCore.contract.Exceptions.NotInterfaceType;
import imcCore.contract.ImcClass;

import java.io.IOException;
import java.net.Socket;

class NonPersistentContractCaller extends ContractCaller {
    NonPersistentContractCaller(String hostName, int port, ImcClass interfaceType, Socket client, int version) throws NotContractInterfaceType, NotInterfaceType, IOException {
        super(hostName, port, interfaceType, client, version);
        close();
    }

    @Override
    void finishMethodHandle() {
        close();
    }

    @Override
    void handleConnectDescription() throws IOException {
        funcConnect();
        readFlags(input);
        matchImcClassDesc();
    }
}
