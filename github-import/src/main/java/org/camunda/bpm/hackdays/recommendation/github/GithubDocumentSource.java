package org.camunda.bpm.hackdays.recommendation.github;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.camunda.bpm.hackdays.recommendation.CamundaSourceDocument;
import org.camunda.bpm.hackdays.recommendation.SolrDocumentSource;

public class GithubDocumentSource implements SolrDocumentSource {

  protected static final String PROPERTIES_FILE = "github.properties";
  protected static final String API_TOKEN = "api-token";
  protected static final String REPOSITORIES = "repositories";
  protected static final String FILE_PATTERN = "file-pattern";
  
  protected static final Pattern REPOSITORY_PART_PATTERN = Pattern.compile("(.*)/(.*)\\[(.*)\\]");
  


  protected Properties properties;
  
  public String getName() {
    return "github-markdown";
  }

  public Iterator<CamundaSourceDocument> documentsIt() {

    properties = loadProperties();
    
    List<CandidateRepository> repositoriesToFetch = extractRepositories();
    
    List<GithubMarkdownDocumentFetcher> fetchers = new ArrayList<GithubMarkdownDocumentFetcher>();
    
    for (CandidateRepository repository : repositoriesToFetch) {
      fetchers.add(new GithubMarkdownDocumentFetcher(
          properties.getProperty(API_TOKEN),
          repository.getRepositoryOwner(), 
          repository.getRepositoryName(),
          repository.getRepositoryType(),
          properties.getProperty(FILE_PATTERN)));
    }
    
    final Iterator<GithubMarkdownDocumentFetcher> repositoryIterator = fetchers.iterator();
    
    return new Iterator<CamundaSourceDocument>() {
      
      Iterator<CamundaSourceDocument> currentRepositoryIterator;

      public boolean hasNext() {
        return (currentRepositoryIterator != null && currentRepositoryIterator.hasNext())
            || repositoryIterator.hasNext(); // TODO: the second condition is not exactly correct
                                             // in case there is a repository left that has no matching files
      }

      public CamundaSourceDocument next() {
        if (currentRepositoryIterator != null && currentRepositoryIterator.hasNext()) {
          CamundaSourceDocument nextDocument = currentRepositoryIterator.next();
          
          return nextDocument;
        }
        else if (repositoryIterator.hasNext()) {
          currentRepositoryIterator = repositoryIterator.next().documents();
          return next();
        }
        throw new NoSuchElementException("no more elements");
      }

      public void remove() {
        throw new UnsupportedOperationException();
        
      }
      
    };
  }
  
  protected List<CandidateRepository> extractRepositories() {
    List<CandidateRepository> repositories = new ArrayList<CandidateRepository>();
    
    String repositoryString = properties.getProperty(REPOSITORIES);
    
    String[] repositoryParts = repositoryString.split(",");
    for (String repositoryPart : repositoryParts) {
      Matcher matcher = REPOSITORY_PART_PATTERN.matcher(repositoryPart);
      
      if (!matcher.matches()) {
        throw new RuntimeException("Could not parse repository configuration; Expecting a"
            + " comma-separated list of 'owner/name[key=value]'");
      }
      
      String repositoryOwner = matcher.group(1);
      String repositoryName = matcher.group(2);
      String metaData = matcher.group(3);
      
      String[] metaDataParts = metaData.split(",");
      String repositoryType = null;
      for (String metaDataPart : metaDataParts) {
        String[] metaDataElement = metaDataPart.split("=");
        if ("type".equals(metaDataElement[0])) {
          repositoryType = metaDataElement[1];
        }
      }
      
      repositories.add(new CandidateRepository(repositoryOwner, repositoryName, repositoryType));
    }
    
    return repositories;
  }
  
  protected Properties loadProperties() {
    Properties properties = new Properties();
    
    try {
      InputStream stream = GithubDocumentSource.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
      properties.load(stream);
      stream.close();
    } catch (Exception e) {
      throw new RuntimeException("could not read properties file", e);
    }
    
    return properties;
  }

}
