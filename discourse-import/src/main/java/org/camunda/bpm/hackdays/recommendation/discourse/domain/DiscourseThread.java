package org.camunda.bpm.hackdays.recommendation.discourse.domain;

import java.util.List;

public class DiscourseThread {

  protected List<DiscoursePost> posts;
  protected int id;
  protected String slug;
  protected String title;
  
  public List<DiscoursePost> getPosts() {
    return posts;
  }

  public void setPosts(List<DiscoursePost> posts) {
    this.posts = posts;
  }
  
  public String getRelativeLink() {
    return "t/" + slug + "/" + id;
  }
  
  public int getId() {
    return id;
  }
  
  public void setId(int id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }
}
