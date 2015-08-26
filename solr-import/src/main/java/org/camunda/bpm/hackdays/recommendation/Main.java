package org.camunda.bpm.hackdays.recommendation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

  public static final Logger LOG = LoggerFactory.getLogger(Main.class);
  
  public static final int DOC_CACHE_SIZE = 100;
  
  public static int ID_COUNTER = 0;
  
  public static void main(String[] args) {
    String solrUrl = args[0];

    SolrClient client = new HttpSolrClient(solrUrl);
    
    try {
      importDocuments(client);
      client.commit();
    } catch (SolrServerException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
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
      LOG.info("Processing source " + documentSource.getName());
      
      
      Iterator<CamundaDocument> documents = documentSource.documentsIt();
      
      List<SolrInputDocument> cachedDocuments = new ArrayList<SolrInputDocument>();
      try {
        while (documents.hasNext()) {
          cachedDocuments.add(solrDocumentFromCamundaDocument(documents.next()));
          
          if (cachedDocuments.size() % DOC_CACHE_SIZE == 0) {
            client.add(cachedDocuments);
            client.commit();
            cachedDocuments.clear();
          }
        }
        
        client.add(cachedDocuments);
        client.commit();
        cachedDocuments.clear();
        
      } catch (SolrServerException e) {
        throw new RuntimeException("Could not connect to solr server", e);
      } catch (IOException e) {
        // TODO: could be more tolerant and just skip in this case
        throw new RuntimeException("could not import documents from source " + documentSource.getName(), e);
      }
    }
  }
  
  protected static SolrInputDocument solrDocumentFromCamundaDocument(CamundaDocument camundaDocument) {
    SolrInputDocument document = new SolrInputDocument();
    document.addField("id", ID_COUNTER++);
    document.addField("link", camundaDocument.getLink());
    document.addField("text", camundaDocument.getContent());
    document.addField("title", camundaDocument.getTitle());
    
    return document;
  }
  

}
