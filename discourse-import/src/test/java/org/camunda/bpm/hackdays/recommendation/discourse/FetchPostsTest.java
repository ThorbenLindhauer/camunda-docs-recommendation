package org.camunda.bpm.hackdays.recommendation.discourse;

import java.util.Iterator;

import org.apache.solr.common.SolrInputDocument;
import org.junit.Assert;
import org.junit.Test;

public class FetchPostsTest {

  @Test
  public void testFetchPosts() {
    
    DiscourseSolrDocumentSource documentSource = new DiscourseSolrDocumentSource();
    
    Iterator<SolrInputDocument> documentsIt = documentSource.documentsIt();
    
    while (documentsIt.hasNext()) {
      SolrInputDocument document = documentsIt.next();
      Assert.assertTrue(document.getField("text").getValue() instanceof String);
      System.out.println(document.getField("title"));
    }
  }
}
