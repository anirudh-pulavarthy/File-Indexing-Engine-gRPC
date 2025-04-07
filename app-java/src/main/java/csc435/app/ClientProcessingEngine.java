package csc435.app;

import csc435.app.FileRetrievalEngineGrpc.FileRetrievalEngineBlockingStub;
import csc435.app.FileRetrievalEngineGrpc;
// import csc435.app.FileRetrievalEngineOuterClass;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

class IndexResult {
    public double executionTime;
    public long totalBytesRead;

    public IndexResult(double executionTime, long totalBytesRead) {
        this.executionTime = executionTime;
        this.totalBytesRead = totalBytesRead;
    }
}

class DocPathFreqPair {
    public String documentPath;
    public long wordFrequency;

    public DocPathFreqPair(String documentPath, long wordFrequency) {
        this.documentPath = documentPath;
        this.wordFrequency = wordFrequency;
    }
}

class SearchResult {
    public double excutionTime;
    public ArrayList<DocPathFreqPair> documentFrequencies;

    public SearchResult(double executionTime, ArrayList<DocPathFreqPair> documentFrequencies) {
        this.excutionTime = executionTime;
        this.documentFrequencies = documentFrequencies;
    }
}

public class ClientProcessingEngine {
    private ManagedChannel channel;
    private FileRetrievalEngineBlockingStub stub;
    private long clientId;

    public ClientProcessingEngine() {}

    public void connect(String serverIP, String serverPort) {
        System.out.println("Trying to connect to the server at " + serverIP + ":" + serverPort);
        channel = ManagedChannelBuilder.forAddress(serverIP, Integer.parseInt(serverPort))
                .usePlaintext()
                .build();
        stub = FileRetrievalEngineGrpc.newBlockingStub(channel);
        
        RegisterRep response = stub.register(com.google.protobuf.Empty.getDefaultInstance());
        clientId = response.getClientId();
        System.out.println("Connected with Client ID: " + clientId);
    }

    private List<File> getAllFiles(File folder) {
        List<File> fileList = new ArrayList<>();
        File[] files = folder.listFiles();
        
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    fileList.add(file);
                } else if (file.isDirectory()) {
                    fileList.addAll(getAllFiles(file));
                }
            }
        }
        return fileList;
    }


    public IndexResult indexFiles(String folderPath) {
        long startTime = System.nanoTime();
        long totalBytesRead = 0;

        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Invalid folder path or not a directory: " + folderPath);
            return new IndexResult(0, 0);
        }

        List<File> files = getAllFiles(folder);
        if (files.isEmpty()) {
            System.out.println("No valid files found in folder222: " + folderPath);
            return new IndexResult(0, 0);
        }

        for (File file : files) {
            // System.out.println("Processing file: " + file.getName());
            try {
                String content = new String(Files.readAllBytes(Paths.get(file.getPath())));
                totalBytesRead += content.length();

                Map<String, Long> wordFrequencies = new HashMap<>();
                for (String word : content.split("[^a-zA-Z0-9_-]+")) {
                    if (word.length() > 3) {
                        wordFrequencies.put(word, wordFrequencies.getOrDefault(word, 0L) + 1);
                    }
                }

                IndexReq request = IndexReq.newBuilder()
                        .setClientId((int) clientId)
                        .setDocumentPath(file.getPath())
                        .putAllWordFrequencies(wordFrequencies)
                        .build();
                stub.computeIndex(request);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        long endTime = System.nanoTime();
        double executionTime = (endTime - startTime) / 1e9;
        return new IndexResult(executionTime, totalBytesRead);
    }

    public SearchResult searchFiles(ArrayList<String> terms) {
        long startTime = System.nanoTime();
        SearchReq request = SearchReq.newBuilder()
                .addAllTerms(terms)
                .build();
        
        SearchRep response = stub.computeSearch(request);
        
        ArrayList<DocPathFreqPair> results = new ArrayList<>();
        for (Map.Entry<String, Long> entry : response.getSearchResultsMap().entrySet()) {
            results.add(new DocPathFreqPair(entry.getKey(), entry.getValue()));
        }
        
        long endTime = System.nanoTime();
        double executionTime = (endTime - startTime) / 1e9;
        return new SearchResult(executionTime, results);
    }

    public long getInfo() {
        return clientId;
    }
}