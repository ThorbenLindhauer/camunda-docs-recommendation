package org.camunda.bpm.hackdays.recommendation.discourse;

import org.camunda.bpm.hackdays.recommendation.CamundaSourceDocument;
import org.camunda.bpm.hackdays.recommendation.discourse.domain.DiscoursePost;
import org.camunda.bpm.hackdays.recommendation.discourse.domain.DiscourseThread;

public class DiscourseDocument implements CamundaSourceDocument {

  public static final String DISCOURSE_HOST = "host";
  
  protected String discourseHost;
  protected DiscourseThread thread;
  
  public DiscourseDocument(String discourseHost, DiscourseThread thread) {
    this.discourseHost = discourseHost;
    this.thread = thread;
  }
  
  public String getLink() {
    return "http://" + discourseHost + "/" + thread.getRelativeLink();
  }

  public String getContent() {
    return concatenatePosts(thread);
  }

  public String getTitle() {
    return thread.getTitle();
  }
  
  public String getType() {
    return "Forum Discussion";
  }
  
  protected String concatenatePosts(DiscourseThread thread) {
    StringBuilder sb = new StringBuilder();
    
    for (DiscoursePost post : thread.getPosts()) {
      sb.append(post.getTextContent());
    }
    
    return sb.toString();
  }

}
