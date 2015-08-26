package org.camunda.bpm.hackdays.recommendation;

import java.util.Iterator;

import org.apache.solr.common.SolrInputDocument;

public interface SolrDocumentSource {

  String getName();
  
  Iterator<SolrInputDocument> documentsIt();
}
