package com.camunda.consulting.camundaDocsIndexer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

public class DatabaseLoader {

  private Client elasticsearchClient;

  public static void main(String[] args) {
    DatabaseLoader loader = new DatabaseLoader();
    loader.run();
    
    loader.shutdown();
  }
  
  public void run() {
    // on startup

    elasticsearchClient = new TransportClient()
            .addTransportAddress(new InetSocketTransportAddress("localhost", 9300));

    runThroughDocs();
  }
  
  public void runThroughDocs() {
    Path userGuideRoot = Paths.get("c:/Users/richtsmeier/git/docs.camunda.org/content", "user-guide");
    
    try {
      DocumentFileVisitor visitor = new DocumentFileVisitor(elasticsearchClient);
      Files.walkFileTree(userGuideRoot, visitor);
    } catch (IOException e1) {
    }
  }

  public void shutdown() {
    // on shutdown
    
    elasticsearchClient.close();    
  }
  
}
