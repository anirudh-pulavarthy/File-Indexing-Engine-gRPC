package csc435.app;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;

public class ServerProcessingEngine {
    private IndexStore store;
    private RPCServerWorker serverWorker;
    private Thread serverThread;

    public ServerProcessingEngine(IndexStore store) {
        this.store = store;
    }

    public void initialize(int serverPort) {
        System.out.println("Initializing gRPC server on port: " + serverPort);
        
        serverWorker = new RPCServerWorker(store, serverPort);
        serverThread = new Thread(serverWorker);
        serverThread.start();
        
        System.out.println("Server initialized on port: " + serverPort);
    }

    public void shutdown() {
        if (serverWorker != null) {
            serverWorker.shutdown();
            System.out.println("Server is shutting down...");
        }
        try {
            if (serverThread != null) {
                serverThread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
