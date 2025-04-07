package csc435.app;

import io.grpc.stub.StreamObserver;
import java.util.concurrent.atomic.AtomicLong;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

public class FileRetrievalEngineService extends FileRetrievalEngineGrpc.FileRetrievalEngineImplBase {
    private final IndexStore store;
    private final AtomicLong clientIdCounter;
    private final Map<Long, String> clientRegistry;
    
    public FileRetrievalEngineService(IndexStore store) {
        this.store = store;
        this.clientIdCounter = new AtomicLong(1);
        this.clientRegistry = new HashMap<>();
    }

    @Override
    public void register(com.google.protobuf.Empty request, StreamObserver<RegisterRep> responseObserver) {
        RegisterRep response = doRegister();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void computeIndex(IndexReq request, StreamObserver<IndexRep> responseObserver) {
        IndexRep response = doIndex(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void computeSearch(SearchReq request, StreamObserver<SearchRep> responseObserver) {
        SearchRep response = doSearch(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private RegisterRep doRegister() {
        int clientId = (int) clientIdCounter.getAndIncrement();
        clientRegistry.put((long) clientId, "Client " + clientId);
        
        return RegisterRep.newBuilder().setClientId(clientId).build();
    }

    private IndexRep doIndex(IndexReq request) {
        // Modify the document path to include "Client X:" prefix before storing
        String modifiedDocPath = "Client " + request.getClientId() + ":" + request.getDocumentPath();
        long documentNumber = store.putDocument(modifiedDocPath);
        store.updateIndex(documentNumber, new HashMap<>(request.getWordFrequenciesMap()));
        
        return IndexRep.newBuilder().setAck("Indexing successful").build();
    }

    private SearchRep doSearch(SearchReq request) {
        Map<String, Long> searchResults = new HashMap<>();
        Set<Long> validDocuments = null;
        
        for (String term : request.getTermsList()) {
            Map<Long, Long> termResults = new HashMap<>();
            for (DocFreqPair docFreq : store.lookupIndex(term)) {
                termResults.put(docFreq.documentNumber, docFreq.wordFrequency);
            }
            
            if (validDocuments == null) {
                validDocuments = new HashSet<>(termResults.keySet());
            } else {
                validDocuments.retainAll(termResults.keySet()); // Intersection for AND logic
            }
        }
        
        if (validDocuments != null) {
            for (Long docNum : validDocuments) {
                String fullPath = store.getDocument(docNum);
                long combinedFrequency = request.getTermsList().stream()
                    .map(term -> store.lookupIndex(term).stream()
                        .filter(df -> df.documentNumber == docNum)
                        .mapToLong(df -> df.wordFrequency).sum())
                    .mapToLong(Long::longValue).sum();
                searchResults.put(fullPath, combinedFrequency);
            }
        }
        
        // Sort the search results in decreasing order of frequency and get total count
        int totalResults = searchResults.size();
        Map<String, Long> sortedResults = searchResults.entrySet().stream()
            .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
            .limit(10)
            .collect(Collectors.toMap(
                Map.Entry::getKey, 
                Map.Entry::getValue, 
                (e1, e2) -> e1, 
                LinkedHashMap::new
            ));
        
        // Print search results header
        System.out.println("Search results (top " + sortedResults.size() + " out of " + totalResults + "):");
        
        return SearchRep.newBuilder().putAllSearchResults(sortedResults).build();
    }
}
