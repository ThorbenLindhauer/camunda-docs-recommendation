package com.camunda.consulting.camundaDocsIndexer;

public class Document {
  
  private String title;
  private String baseUrl;
  private String contentUrl;
  private String url;
  private String content;
  
  public Document(String title, String baseUrl, String contentUrl, String content) {
    super();
    this.title = title;
    this.baseUrl = baseUrl;
    this.contentUrl = contentUrl;
    this.content = content;
    if (baseUrl != null && baseUrl.isEmpty() == false && contentUrl != null && contentUrl.isEmpty() == false) {
      this.url = baseUrl + "/" + contentUrl;
    } else {
      this.url = "";
    }
  }
  
  public Document() {
    super();
  }
  
  public String getTitle() {
    return title;
  }
  public void setTitle(String title) {
    this.title = title;
  }
  public String getBaseUrl() {
    return baseUrl;
  }
  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }
  public String getContentUrl() {
    return contentUrl;
  }
  public void setContentUrl(String contentUrl) {
    this.contentUrl = contentUrl;
  }
  public String getUrl() {
    return url;
  }
  public void setUrl(String url) {
    this.url = url;
  }
  public String getContent() {
    return content;
  }
  public void setContent(String content) {
    this.content = content;
  }

  @Override
  public String toString() {
    return "Document [title=" + title + 
        ", baseUrl=" + baseUrl + 
        ", contentUrl=" + contentUrl + 
        ", url=" + url + 
        ", content=" + (content.length() > 29 ? content.substring(0, 30)+"..." : content) + 
        "]";
  }
  
  

}
