package csc435.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// Data structure that stores a document number and the number of times a word/term appears in the document
class DocFreqPair {
    public long documentNumber;
    public long wordFrequency;

    public DocFreqPair(long documentNumber, long wordFrequency) {
        this.documentNumber = documentNumber;
        this.wordFrequency = wordFrequency;
    }
}

public class IndexStore {
    private final HashMap<String, Long> documentMap;
    private final ConcurrentHashMap<String, ArrayList<DocFreqPair>> termInvertedIndex;
    private final Lock documentMapLock;
    private final Lock termIndexLock;
    private long documentCounter;

    public IndexStore() {
        this.documentMap = new HashMap<>();
        this.termInvertedIndex = new ConcurrentHashMap<>();
        this.documentMapLock = new ReentrantLock();
        this.termIndexLock = new ReentrantLock();
        this.documentCounter = 0;
    }

    public long putDocument(String documentPath) {
        documentMapLock.lock();
        try {
            if (!documentMap.containsKey(documentPath)) {
                documentCounter++;
                documentMap.put(documentPath, documentCounter);
                return documentCounter;
            } else {
                return documentMap.get(documentPath);
            }
        } finally {
            documentMapLock.unlock();
        }
    }

    public String getDocument(long documentNumber) {
        documentMapLock.lock();
        try {
            for (var entry : documentMap.entrySet()) {
                if (entry.getValue().equals(documentNumber)) {
                    return entry.getKey();
                }
            }
            return "";
        } finally {
            documentMapLock.unlock();
        }
    }

    public void updateIndex(long documentNumber, HashMap<String, Long> wordFrequencies) {
        termIndexLock.lock();
        try {
            for (var entry : wordFrequencies.entrySet()) {
                String term = entry.getKey();
                long frequency = entry.getValue();
                
                termInvertedIndex.putIfAbsent(term, new ArrayList<>());
                termInvertedIndex.get(term).add(new DocFreqPair(documentNumber, frequency));
            }
        } finally {
            termIndexLock.unlock();
        }
    }

    public ArrayList<DocFreqPair> lookupIndex(String term) {
        return termInvertedIndex.getOrDefault(term, new ArrayList<>());
    }
}