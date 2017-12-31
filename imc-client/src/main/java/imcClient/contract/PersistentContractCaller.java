package imcClient.contract;

import imcCore.contract.ImcClass;

import java.io.IOException;
import java.net.Socket;

class PersistentContractCaller extends ContractCaller {
    PersistentContractCaller(String hostName, int port, ImcClass interfaceType, Socket client, int version) throws IOException {
        super(hostName, port, interfaceType, client, version);
        matchImcClassDesc();
    }

    @Override
    void handleConnectDescription() {

    }

    @Override
    void finishMethodHandle() {

    }
}
