package org.camunda.bpm.hackdays.recommendation.discourse;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

import org.apache.http.HttpHost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.solr.common.SolrInputDocument;
import org.camunda.bpm.hackdays.recommendation.SolrDocumentSource;
import org.camunda.bpm.hackdays.recommendation.discourse.domain.DiscoursePost;
import org.camunda.bpm.hackdays.recommendation.discourse.domain.DiscourseThread;

public class DiscourseSolrDocumentSource implements SolrDocumentSource {

  public static final String PROPERTIES_FILE = "discourse.properties";
  
  // TODO Make configurable
  public static final String DISCOURSE_HOST = "host";
  public static final String DISCOURSE_CATEGORY = "category";
  public static final String DISCOURSE_API_KEY = "api-key";
  
  public static final String SEQUENCE_NUMBER = "sequence";
  
  protected Properties properties;

  public DiscourseSolrDocumentSource() {
    properties = loadProperties();
  }
  
  public String getName() {
    return "discourse";
  }

  public Iterator<SolrInputDocument> documentsIt() {
    CloseableHttpClient client = HttpClients.createDefault();
    
    HttpHost host = new HttpHost(properties.getProperty(DISCOURSE_HOST));
    
    final DiscoursePostFetcher fetcher = 
        new DiscoursePostFetcher(
            client, 
            host, 
            properties.getProperty(DISCOURSE_API_KEY), 
            properties.getProperty(DISCOURSE_CATEGORY), 
            Integer.parseInt(properties.getProperty(SEQUENCE_NUMBER)));
    
    return new Iterator<SolrInputDocument>() {
      Iterator<DiscourseThread> threadIt = fetcher.threads();

      public boolean hasNext() {
        return threadIt.hasNext();
      }

      public SolrInputDocument next() {
        DiscourseThread nextThread = threadIt.next();
        
        SolrInputDocument document = new SolrInputDocument();
        document.addField("id", nextThread.getId());
        document.addField("link", "http://" + properties.getProperty(DISCOURSE_HOST) + "/" + nextThread.getRelativeLink());
        document.addField("text", concatenatePosts(nextThread));
        document.addField("title", nextThread.getTitle());
        
        return document;
      }

      public void remove() {
        threadIt.remove();
      }
    };
  }
  
  protected String concatenatePosts(DiscourseThread thread) {
    StringBuilder sb = new StringBuilder();
    
    for (DiscoursePost post : thread.getPosts()) {
      sb.append(post.getTextContent());
    }
    
    return sb.toString();
  }
  
  protected Properties loadProperties() {
    Properties properties = new Properties();
    
    try {
      InputStream stream = DiscourseSolrDocumentSource.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
      properties.load(stream);
    } catch (Exception e) {
      throw new RuntimeException("could not read properties file", e);
    }
    
    return properties;
  }

}
