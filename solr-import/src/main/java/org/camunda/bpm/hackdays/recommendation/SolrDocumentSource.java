package org.camunda.bpm.hackdays.recommendation;

import java.util.Iterator;

public interface SolrDocumentSource {

  String getName();
  
  Iterator<CamundaDocument> documentsIt();
}
