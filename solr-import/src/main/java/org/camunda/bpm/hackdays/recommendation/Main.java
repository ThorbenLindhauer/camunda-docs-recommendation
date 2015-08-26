package org.camunda.bpm.hackdays.recommendation;

import java.io.IOException;
import java.util.Iterator;
import java.util.ServiceLoader;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;

public class Main {

  public static void main(String[] args) {
    String solrUrl = args[0];

    SolrClient client = new HttpSolrClient(solrUrl);
    
    try {
      importDocuments(client);
    } finally {
      try {
        client.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
  
  protected static void importDocuments(SolrClient client) {
    ServiceLoader<SolrDocumentSource> documentSources = ServiceLoader.load(SolrDocumentSource.class);
    
    for (SolrDocumentSource documentSource : documentSources) {
      Iterator<SolrInputDocument> documents = documentSource.documentsIt();
      try {
        client.add(documents);
        
      } catch (SolrServerException e) {
        throw new RuntimeException("Could not connect to solr server", e);
      } catch (IOException e) {
        // TODO: could be more tolerant and just skip in this case
        throw new RuntimeException("could not import documents from source " + documentSource.getName(), e);
      }
    }
  }

}
