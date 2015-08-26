package org.camunda.bpm.hackdays.recommendation.discourse;

import java.util.Iterator;

import org.apache.solr.common.SolrInputDocument;
import org.camunda.bpm.hackdays.recommendation.SolrDocumentSource;

public class DiscourseSolrDocumentSource implements SolrDocumentSource {

  public String getName() {
    return "discourse";
  }

  public Iterator<SolrInputDocument> documentsIt() {
    // TODO Auto-generated method stub
    return null;
  }

}
