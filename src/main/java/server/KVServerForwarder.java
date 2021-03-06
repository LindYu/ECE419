package server;

import common.connection.AbstractKVConnection;
import common.messages.AbstractKVMessage;
import common.messages.KVMessage;
import common.messages.TextMessage;
import ecs.ECSNode;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * This class will forward the put requests from coordinator server to
 * replication servers
 */
public class KVServerForwarder extends AbstractKVConnection {
    private String name;
    public static final List<KVMessage.StatusType> successStatus = Arrays.asList(
            KVMessage.StatusType.PUT_SUCCESS,
            KVMessage.StatusType.PUT_UPDATE,
            KVMessage.StatusType.DELETE_SUCCESS,
            KVMessage.StatusType.SQL_SUCCESS
    );

    public KVServerForwarder(ECSNode node) {
        assert node != null;
        this.address = node.getNodeHost();
        this.port = node.getNodePort();
        this.name = node.getNodeName();
    }

    public String getName() {
        return name;
    }

    public void forward(KVMessage message) throws IOException, ForwardFailedException {
        AbstractKVMessage req = AbstractKVMessage.createMessage();
        AbstractKVMessage res = AbstractKVMessage.createMessage();

        assert req != null;
        assert res != null;
        req.setKey(message.getKey());
        switch (message.getStatus()) {
            case SQL:
                req.setStatus(KVMessage.StatusType.SQL_REPLICATE);
                break;
            case PUT:
                req.setStatus(KVMessage.StatusType.PUT_REPLICATE);
                break;
            default:
                throw new ForwardFailedException("Must forward put/sql request! but get "
                        + message.getStatus());
        }
        req.setValue(message.getValue());

        sendMessage(new TextMessage(req.encode()));
        res.decode(receiveMessage().getMsg());

        if (!successStatus.contains(res.getStatus())) {
            throw new ForwardFailedException(
                    "Forward to server at " + this.address + ":" + this.port + " failed " + res);
        }
    }

    public static class ForwardFailedException extends Exception {
        public ForwardFailedException(String msg) {
            super(msg);
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return "KVServerForwarder{" +
                "address='" + address + '\'' +
                ", port=" + port +
                '}';
    }
}
