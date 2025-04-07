package csc435.app;

import java.util.ArrayList;
import java.util.Scanner;

public class ClientAppInterface {
    private ClientProcessingEngine engine;

    public ClientAppInterface(ClientProcessingEngine engine) {
        this.engine = engine;
    }

    public void readCommands() {
        Scanner sc = new Scanner(System.in);
        String command;

        while (true) {
            System.out.print("> ");
            command = sc.nextLine().trim();

            if (command.equals("quit")) {
                System.out.println("Client shutting down...");
                break;
            }

            if (command.startsWith("connect")) {
                String[] parts = command.split(" ");
                if (parts.length == 3) {
                    engine.connect(parts[1], parts[2]);
                    System.out.println("Connected to server at " + parts[1] + ":" + parts[2]);
                } else {
                    System.out.println("Usage: connect <server IP> <server port>");
                }
                continue;
            }

            if (command.equals("get_info")) {
                long clientId = engine.getInfo();
                System.out.println("Client ID: " + clientId);
                continue;
            }

            if (command.startsWith("index")) {
                String[] parts = command.split(" ", 2);
                if (parts.length == 2) {
                    IndexResult result = engine.indexFiles(parts[1]);
                    System.out.println("Indexing completed in " + result.executionTime + " seconds, " + result.totalBytesRead + " bytes read.");
                } else {
                    System.out.println("Usage: index <folder path>");
                }
                continue;
            }

            if (command.startsWith("search")) {
                String[] parts = command.split(" ", 2);
                if (parts.length == 2) {
                    String[] terms = parts[1].split(" AND ");
                    ArrayList<String> searchTerms = new ArrayList<>();
                    for (String term : terms) {
                        searchTerms.add(term.trim());
                    }
                    SearchResult result = engine.searchFiles(searchTerms);
                    System.out.println("Search completed in " + result.excutionTime + " seconds.");
                    for (DocPathFreqPair doc : result.documentFrequencies) {
                        System.out.println(doc.documentPath + " (Frequency: " + doc.wordFrequency + ")");
                    }
                } else {
                    System.out.println("Usage: search <term1> AND <term2> AND <term3>");
                }
                continue;
            }

            System.out.println("Unrecognized command!");
        }

        sc.close();
    }
}