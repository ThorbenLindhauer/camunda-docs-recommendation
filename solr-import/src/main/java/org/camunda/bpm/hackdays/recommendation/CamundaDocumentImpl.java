package org.camunda.bpm.hackdays.recommendation;

public class CamundaDocumentImpl implements CamundaDocument {
  
  protected int id;
  protected CamundaSourceDocument sourceDocument;

  public CamundaDocumentImpl(int id, CamundaSourceDocument sourceDocument) {
    this.id = id;
    this.sourceDocument = sourceDocument;
  }
  
  public String getLink() {
    return sourceDocument.getLink();
  }

  public String getContent() {
    return sourceDocument.getContent();
  }

  public String getTitle() {
    return sourceDocument.getTitle();
  }

  public int getId() {
    return id;
  }
  
  public void setId(int id) {
    this.id = id;
  }

}
