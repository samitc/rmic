package imcClient.contract;

import imcCore.contract.Exceptions.NotContractInterfaceType;
import imcCore.contract.Exceptions.NotInterfaceType;
import imcCore.contract.ImcClass;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

class PersistentContractCaller extends ContractCaller {
    PersistentContractCaller(String hostName, int port, ImcClass interfaceType, Socket client, int version) throws NotContractInterfaceType, NotInterfaceType, IOException {
        super(hostName, port, interfaceType, client, version);
        input = new DataInputStream(client.getInputStream());
        output = new DataOutputStream(client.getOutputStream());
        matchImcClassDesc();
    }

    @Override
    void handleConnectDescription() throws IOException {

    }

    @Override
    void finishMethodHandle() {

    }
}
