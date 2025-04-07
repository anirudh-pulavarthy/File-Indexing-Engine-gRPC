package csc435.app;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;

public class RPCServerWorker implements Runnable {
    private final IndexStore store;
    private final int serverPort;
    private Server server;

    public RPCServerWorker(IndexStore store, int serverPort) {
        this.store = store;
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        try {
            server = ServerBuilder.forPort(serverPort)
                    .addService(new FileRetrievalEngineService(store))
                    .build()
                    .start();
            System.out.println("gRPC Server started on port " + serverPort);
            server.awaitTermination();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        if (server != null) {
            server.shutdown();
            System.out.println("gRPC Server shutting down...");
        }
    }
}
