package org.camunda.bpm.hackdays.recommendation.discourse;

import static org.camunda.spin.Spin.JSON;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.camunda.bpm.hackdays.recommendation.discourse.domain.DiscoursePost;
import org.camunda.bpm.hackdays.recommendation.discourse.domain.DiscourseThread;
import org.camunda.spin.SpinList;
import org.camunda.spin.json.SpinJsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spinjar.com.fasterxml.jackson.databind.JsonNode;

public class DiscoursePostFetcher {
  
  public static final Logger LOG = LoggerFactory.getLogger(DiscoursePostFetcher.class);
  
  protected HttpClient client;
  protected HttpHost host;
  protected String apiKey;
  protected String category;
  protected int maxSequence;

  public DiscoursePostFetcher(HttpClient client, HttpHost host, String apiKey, String category, int sequence) {
    this.client = client;
    this.host = host;
    this.apiKey = apiKey;
    this.category = category;
    this.maxSequence = sequence;
  }
  
  public Iterator<DiscourseThread> threads() {
    return new ThreadIterator();
  }
  
  public class ThreadIterator implements Iterator<DiscourseThread> {
    
    protected int currentSequence = 0;
    
    protected DiscourseThread currentThread = null;
    
    public ThreadIterator() {
      move();
    }
    
    public boolean hasNext() {
      return currentSequence < maxSequence;
    }

    public DiscourseThread next() {
      DiscourseThread thread = currentThread;
      move();
      return thread;
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
    
    protected void move() {
      boolean threadLoaded = false;
      while (!threadLoaded && currentSequence < maxSequence) {
        try {
          currentSequence++;
          LOG.info("loading document with sequence id " + currentSequence);
          currentThread = loadThread(currentSequence);
          threadLoaded = true;
        } catch (Exception e) {
          LOG.warn("Could not load thread " + currentSequence + ". Skipping it", e);
        }
      }
    }
  }
  
  protected DiscourseThread loadThread(int sequenceId) throws Exception {
    HttpRequest request = new HttpGet("/t/" + sequenceId + ".json?api_key=" + apiKey);
    
    HttpResponse response = client.execute(host, request);
    
    HttpEntity entity = response.getEntity();
    String encoding = entity.getContentEncoding() != null ? entity.getContentEncoding().getValue() : "UTF-8";
    
    InputStreamReader reader = new InputStreamReader(entity.getContent(), encoding);
    SpinJsonNode jsonNode = JSON(reader);
    
    SpinList<SpinJsonNode> posts = jsonNode.prop("post_stream").prop("posts").elements();
    
    List<DiscoursePost> discoursePosts = new ArrayList<DiscoursePost>();
    for (SpinJsonNode post : posts) {
      String postContent = post.prop("cooked").stringValue();
      DiscoursePost discoursePost = new DiscoursePost();
      discoursePost.setTextContent(postContent);
      discoursePosts.add(discoursePost);
    }
    
    DiscourseThread thread = new DiscourseThread();
    thread.setId(sequenceId);
    thread.setPosts(discoursePosts);
    thread.setSlug(jsonNode.prop("slug").stringValue());
    thread.setTitle(jsonNode.prop("title").stringValue());
    thread.setDeleted(((JsonNode) jsonNode.prop("deleted_at").unwrap()).isNull() == false);
    return thread;
  }
}
