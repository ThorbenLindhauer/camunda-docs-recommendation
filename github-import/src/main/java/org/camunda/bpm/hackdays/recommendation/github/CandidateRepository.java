package org.camunda.bpm.hackdays.recommendation.github;

public class CandidateRepository {

  protected String repositoryOwner;
  protected String repositoryName;
  
  public CandidateRepository(String repositoryOwner, String repositoryName) {
    this.repositoryOwner = repositoryOwner;
    this.repositoryName = repositoryName;
  }
  public String getRepositoryOwner() {
    return repositoryOwner;
  }
  public void setRepositoryOwner(String repositoryOwner) {
    this.repositoryOwner = repositoryOwner;
  }
  public String getRepositoryName() {
    return repositoryName;
  }
  public void setRepositoryName(String repositoryName) {
    this.repositoryName = repositoryName;
  }
  
}
