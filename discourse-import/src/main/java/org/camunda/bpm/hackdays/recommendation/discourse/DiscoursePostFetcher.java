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

public class DiscoursePostFetcher {
  
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
    
    return new Iterator<DiscourseThread>() {
      int currentSequence = 1;

      public boolean hasNext() {
        return currentSequence < maxSequence;
      }

      public DiscourseThread next() {
        DiscourseThread thread = loadThread(currentSequence);
        currentSequence++;
        return thread;
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }
  
    
  protected DiscourseThread loadThread(int sequenceId) {
    HttpRequest request = new HttpGet("/t/" + sequenceId + ".json?api_key=" + apiKey);
    
    try {
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
      thread.setTitle(jsonNode.prop("title").stringValue());
      return thread;
    } catch (Exception e) {
      // TODO: log warning instead and skip this thread
      throw new RuntimeException("could not load thread", e);
    }
  }
}
