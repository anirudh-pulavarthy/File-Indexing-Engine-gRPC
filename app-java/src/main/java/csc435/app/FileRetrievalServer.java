package csc435.app;

public class FileRetrievalServer {
    public static void main(String[] args) {
        // Ensure a port argument is provided
        if (args.length < 1) {
            System.err.println("Usage: java FileRetrievalServer <port>");
            System.exit(1);
        }

        // Parse and validate the port number
        int serverPort;
        try {
            serverPort = Integer.parseInt(args[0]);
            if (serverPort < 1024 || serverPort > 65535) {
                throw new IllegalArgumentException("Port must be between 1024 and 65535.");
            }
        } catch (Exception e) {
            System.err.println("Invalid port: " + e.getMessage());
            System.exit(1);
            return;
        }

        IndexStore store = new IndexStore();
        ServerProcessingEngine engine = new ServerProcessingEngine(store);
        ServerAppInterface appInterface = new ServerAppInterface(engine);
        
        // Create a thread that runs the gRPC server
        engine.initialize(serverPort);

        // Read commands from the user
        appInterface.readCommands();
    }
}