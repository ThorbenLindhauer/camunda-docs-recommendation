package org.camunda.bpm.hackdays.recommendation.github;

import org.camunda.bpm.hackdays.recommendation.CamundaSourceDocument;

public class GithubMarkdownDocument implements CamundaSourceDocument {

  protected String link;
  protected String content;
  protected String title;
  
  public String getLink() {
    return link;
  }
  
  public void setLink(String link) {
    this.link = link;
  }

  public String getContent() {
    return content;
  }
  
  public void setContent(String content) {
    this.content = content;
  }

  public String getTitle() {
    return title;
  }
  
  public void setTitle(String title) {
    this.title = title;
  }
  
  public String getType() {
    return "Example Project";
  }

}
