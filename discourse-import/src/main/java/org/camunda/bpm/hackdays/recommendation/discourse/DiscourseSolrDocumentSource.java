package org.camunda.bpm.hackdays.recommendation.discourse;

import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Properties;

import org.apache.http.HttpHost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.camunda.bpm.hackdays.recommendation.CamundaSourceDocument;
import org.camunda.bpm.hackdays.recommendation.SolrDocumentSource;
import org.camunda.bpm.hackdays.recommendation.discourse.domain.DiscoursePost;
import org.camunda.bpm.hackdays.recommendation.discourse.domain.DiscourseThread;

public class DiscourseSolrDocumentSource implements SolrDocumentSource {

  public static final String PROPERTIES_FILE = "discourse.properties";
  
  public static final String DISCOURSE_HOST = "host";
  public static final String DISCOURSE_CATEGORY = "category";
  public static final String DISCOURSE_API_KEY = "api-key";
  
  public static final String SEQUENCE_START_NUMBER = "sequence-start";
  public static final String SEQUENCE_END_NUMBER = "sequence-end";
  
  protected Properties properties;

  public DiscourseSolrDocumentSource() {
    properties = loadProperties();
  }
  
  public String getName() {
    return "discourse";
  }

  public Iterator<CamundaSourceDocument> documentsIt() {
    return new DiscourseSolrDocumentIterator(properties);
  }
  
  public static class DiscourseSolrDocumentIterator implements Iterator<CamundaSourceDocument> {
    
    protected DiscoursePostFetcher fetcher;
    protected Properties properties;
    protected CamundaSourceDocument nextDocument;
    protected Iterator<DiscourseThread> threadIt;
    
    public DiscourseSolrDocumentIterator(Properties properties) {
      CloseableHttpClient client = HttpClients.createDefault();
      
      HttpHost host = new HttpHost(properties.getProperty(DISCOURSE_HOST));
      
      this.fetcher = 
          new DiscoursePostFetcher(
              client, 
              host, 
              properties.getProperty(DISCOURSE_API_KEY), 
              properties.getProperty(DISCOURSE_CATEGORY),
              Integer.parseInt(properties.getProperty(SEQUENCE_START_NUMBER)),
              Integer.parseInt(properties.getProperty(SEQUENCE_END_NUMBER)));
      this.threadIt = fetcher.threads();
      this.properties = properties;
      
      move();
    }
    
    
    public boolean hasNext() {
      return nextDocument != null;
    }

    public CamundaSourceDocument next() {
      if (nextDocument == null) {
        throw new NoSuchElementException();
      }
      CamundaSourceDocument returnDocument = nextDocument;
        
      
      move();
      return returnDocument;
    }

    public void remove() {
      threadIt.remove();
    }
    
    protected void move() {
      
      if (threadIt.hasNext()) {
        DiscourseThread nextThread = threadIt.next();
        
        while (nextThread.isDeleted() && threadIt.hasNext()) {
          nextThread = threadIt.next();
        }
        if (!nextThread.isDeleted()) {
          nextDocument = new DiscourseDocument(properties.getProperty(DISCOURSE_HOST), nextThread);
        }
        else {
          nextDocument = null;
        }
      }
      else {
        nextDocument = null;
      }
      
    }
    
    protected String concatenatePosts(DiscourseThread thread) {
      StringBuilder sb = new StringBuilder();
      
      for (DiscoursePost post : thread.getPosts()) {
        sb.append(post.getTextContent());
      }
      
      return sb.toString();
    }
  }
  
  
  protected Properties loadProperties() {
    Properties properties = new Properties();
    
    try {
      InputStream stream = DiscourseSolrDocumentSource.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
      properties.load(stream);
      stream.close();
    } catch (Exception e) {
      throw new RuntimeException("could not read properties file", e);
    }
    
    return properties;
  }

}
