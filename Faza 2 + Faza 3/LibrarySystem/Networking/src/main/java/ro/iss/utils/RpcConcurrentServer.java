package ro.iss.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.iss.services.IService;
import ro.iss.protocol.ClientRpcWorker;

import java.net.Socket;

public class RpcConcurrentServer extends AbsConcurrentServer {
    private IService service;
    private static Logger logger = LogManager.getLogger(RpcConcurrentServer.class);

    public RpcConcurrentServer(int port, IService service) {
        super(port);
        this.service = service;
        logger.info("Chat- ChatRpcConcurrentServer");
    }

    @Override
    protected Thread createWorker(Socket client) {
        ClientRpcWorker worker=new ClientRpcWorker(service, client);

        Thread tw=new Thread(worker);
        return tw;
    }

    @Override
    public void stop(){
        logger.info("Stopping services ...");
    }
}