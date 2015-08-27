package org.camunda.bpm.hackdays.recommendation.discourse;

import java.util.Iterator;

import org.camunda.bpm.hackdays.recommendation.CamundaSourceDocument;
import org.junit.Assert;
import org.junit.Test;

public class FetchPostsTest {

  @Test
  public void testFetchPosts() {
    
    DiscourseSolrDocumentSource documentSource = new DiscourseSolrDocumentSource();
    
    Iterator<CamundaSourceDocument> documentsIt = documentSource.documentsIt();
    
    while (documentsIt.hasNext()) {
      CamundaSourceDocument document = documentsIt.next();
      Assert.assertTrue(document.getContent() != null);
      System.out.println(document.getLink());
    }
  }
}
