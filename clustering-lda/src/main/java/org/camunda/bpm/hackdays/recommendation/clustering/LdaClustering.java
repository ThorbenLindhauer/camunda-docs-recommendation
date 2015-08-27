package org.camunda.bpm.hackdays.recommendation.clustering;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.camunda.bpm.hackdays.recommendation.CamundaDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicAssignment;
import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

public class LdaClustering {
  
  public static final Logger LOG = LoggerFactory.getLogger(LdaClustering.class);

  protected Iterator<CamundaDocument> documentsIt;
  protected int numberOfTopics;
  protected ParallelTopicModel topicModel;
  
  // document id => topic model data id
  protected Map<Integer, Integer> instanceIdMapping;

  public LdaClustering(Iterator<CamundaDocument> documentsIt, int numberOfTopics) {
    this.documentsIt = documentsIt;
    this.numberOfTopics = numberOfTopics;
    this.instanceIdMapping = new HashMap<Integer, Integer>();
  }

  public void build() {
    topicModel = new ParallelTopicModel(numberOfTopics);
    
    InstanceList instances = new InstanceList(getPipe());
    instances.addThruPipe(new InstanceIterator());
    topicModel.addInstances(instances);
    topicModel.setNumThreads(4);
    topicModel.setNumIterations(1000);
    try {
      topicModel.estimate();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    
    ArrayList<TopicAssignment> data = topicModel.getData();
    for (int i = 0; i < data.size(); i++) {
      TopicAssignment assignment = data.get(i);
      instanceIdMapping.put((Integer) assignment.instance.getName(), i);
    }
  }
  
  public double[] getTopicProbabilities(int documentId) {
    int dataId = instanceIdMapping.get(documentId);
    return topicModel.getTopicProbabilities(dataId);
  }

  protected static Pipe getPipe() {
    List<Pipe> pipeList = new ArrayList<Pipe>();

    // Pipes: lowercase, tokenize, remove stopwords, map to features
    pipeList.add(new RemoveHtmlTags());
    pipeList.add(new CharSequenceLowercase());
    pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
    try {
      pipeList.add(new TokenSequenceRemoveStopwords(
          new File(LdaClustering.class.getClassLoader().getResource("stopwords_en.txt").toURI()), 
          "UTF-8", false, false, false));
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
    pipeList.add(new TokenSequence2FeatureSequence());

    return new SerialPipes(pipeList);
  }
  
  public void printSummary() {
    // Show top 5 words in topics with proportions for the first document

    ArrayList<TreeSet<IDSorter>> topicSortedWords = topicModel.getSortedWords();
    Alphabet dataAlphabet = topicModel.getAlphabet();
    
    for (int topic = 0; topic < numberOfTopics; topic++) {
      Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
      
      Formatter out = new Formatter(new StringBuilder(), Locale.US);
      out.format("%d\t", topic);
      int rank = 0;
      while (iterator.hasNext() && rank < 5) {
        IDSorter idCountPair = iterator.next();
        out.format("%s (%.0f) ", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
        rank++;
      }
      LOG.info(out.toString());
      out.close();
    }
    
  }
  
  public class InstanceIterator implements Iterator<Instance> {

    public boolean hasNext() {
      return documentsIt.hasNext();
    }

    public Instance next() {
      CamundaDocument next = documentsIt.next();
      Instance instance = new Instance(next.getContent(), null, next.getId(), null);
      return instance;
    }

    public void remove() {
      throw new UnsupportedOperationException();
      
    }
    
  }
}
