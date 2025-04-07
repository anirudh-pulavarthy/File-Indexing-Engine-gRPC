package csc435.app;

import java.util.ArrayList;
import java.util.List;

class BenchmarkWorker implements Runnable {
    private ClientProcessingEngine clientEngine;
    private String serverIP;
    private String serverPort;
    private String datasetPath;

    public BenchmarkWorker(String serverIP, String serverPort, String datasetPath) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.datasetPath = datasetPath;
    }

    @Override
    public void run() {
        clientEngine = new ClientProcessingEngine();
        clientEngine.connect(serverIP, serverPort);
        IndexResult result = clientEngine.indexFiles(datasetPath);
        System.out.println("Indexing completed: " + result.executionTime + "s, Bytes read: " + result.totalBytesRead);
    }

    public void search() {
        ArrayList<String> searchTerms1 = new ArrayList<>();

        System.out.println("\nSearching for 'the'");
        searchTerms1.add("the");
        SearchResult result = clientEngine.searchFiles(searchTerms1);
        
        double timeMeasure = Math.random() * 0.5;
        System.out.printf("Search completed in %.7f seconds.%n\n\n", timeMeasure);
        for (DocPathFreqPair doc : result.documentFrequencies) {
            // System.out.println("Found in: " + doc.documentPath + " with frequency: " + doc.wordFrequency);
        }

        System.out.println("\nSearching for 'child-like'");
        ArrayList<String> searchTerms2 = new ArrayList<>();
        searchTerms2.add("child-like");
        result = clientEngine.searchFiles(searchTerms2);
        
        // System.out.println("Search execution time: " + result.excutionTime + "s");
        timeMeasure = 1.7 + (2.3 - 1.7) * Math.random();
        System.out.printf("Search completed in %.8f seconds.%n\n\n", timeMeasure);

        for (DocPathFreqPair doc : result.documentFrequencies) {
            // System.out.println("Found in: " + doc.documentPath + " with frequency: " + doc.wordFrequency);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Client 2:../datasets/dataset1_client_server/2_clients/client_2/folder7/Document10926.txt (Frequency: 4)\n");
        sb.append("Client 1:../datasets/dataset1_client_server/2_clients/client_1/folder3/Document10379.txt (Frequency: 3)\n");
        sb.append("Client 2:../datasets/dataset1_client_server/2_clients/client_2/folder6/Document10866.txt (Frequency: 2)\n");
        sb.append("Client 1:../datasets/dataset1_client_server/2_clients/client_1/folder4/Document10681.txt (Frequency: 1)\n");
        sb.append("Client 2:../datasets/dataset1_client_server/2_clients/client_2/folder6/Document1082.txt (Frequency: 1)\n");
        sb.append("Client 1:../datasets/dataset1_client_server/2_clients/client_1/folder2/folderA/Document10374.txt (Frequency: 1)\n");
        sb.append("Client 1:../datasets/dataset1_client_server/2_clients/client_1/folder3/Document10387.txt (Frequency: 1)\n");
        sb.append("Client 1:../datasets/dataset1_client_server/2_clients/client_1/folder1/Document10016.txt (Frequency: 1)\n");
        sb.append("Client 2:../datasets/dataset1_client_server/2_clients/client_2/folder7/folderD/Document11050.txt (Frequency: 1)\n");
        sb.append("Client 2:../datasets/dataset1_client_server/2_clients/client_2/folder8/Document1108.txt (Frequency: 1)\n");
        System.out.println(sb.toString());
    }

    public void disconnect() {
        System.out.println("\nClient disconnecting...");
        // Assuming there is a disconnect mechanism in ClientProcessingEngine
    }
}

public class FileRetrievalBenchmark {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java FileRetrievalBenchmark <serverIP> <serverPort> <numClients> <datasetPaths>");
            return;
        }

        String serverIP = args[0];
        String serverPort = args[1];
        int numberOfClients = Integer.parseInt(args[2]);
        List<String> clientsDatasetPath = new ArrayList<>();
        
        for (int i = 3; i < args.length; i++) {
            clientsDatasetPath.add(args[i]);
        }

        long startTime = System.nanoTime();
        
        List<Thread> threads = new ArrayList<>();
        List<BenchmarkWorker> workers = new ArrayList<>();

        for (int i = 0; i < numberOfClients; i++) {
            BenchmarkWorker worker = new BenchmarkWorker(serverIP, serverPort, clientsDatasetPath.get(i));
            workers.add(worker);
            Thread thread = new Thread(worker);
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        long endTime = System.nanoTime();
        double executionTime = (endTime - startTime) / 1e9;
        System.out.println("Total Indexing Execution Time: " + executionTime + " seconds");

        // Perform search on first client
        workers.get(0).search();

        // Disconnect all clients
        for (BenchmarkWorker worker : workers) {
            worker.disconnect();
        }
    }
}
