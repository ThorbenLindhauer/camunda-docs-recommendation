package org.camunda.bpm.hackdays.recommendation.github;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.camunda.bpm.hackdays.recommendation.CamundaSourceDocument;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryBranch;
import org.eclipse.egit.github.core.Tree;
import org.eclipse.egit.github.core.TreeEntry;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.DataService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.jgit.api.Git;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.pegdown.PegDownProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class GithubMarkdownDocumentFetcher {

  public static final Logger LOG = LoggerFactory.getLogger(GithubMarkdownDocumentFetcher.class);

  protected static final Pattern YAML_HEADER_PATTERN = Pattern.compile("(?s)^\\s*---\\s(.*?)---");

  protected Pattern filePattern;
  protected GitHubClient client;
  protected String repositoryOwner;
  protected String repositoryName;
  protected String repositoryType = "Example Project";
  
  public GithubMarkdownDocumentFetcher(String apiToken, String repositoryOwner, 
      String repositoryName, String repositoryType, String filePattern) {
    this.filePattern = Pattern.compile(filePattern, Pattern.CASE_INSENSITIVE);
    this.client = new GitHubClient();
    this.repositoryOwner = repositoryOwner;
    this.repositoryName = repositoryName;

    if (repositoryType != null) {
      this.repositoryType = repositoryType;
    }
    
    client.setOAuth2Token(apiToken);
  }
  
  public Iterator<CamundaSourceDocument> documents() {
    return new MarkdownDocumentIterator();
  }
  
  public class MarkdownDocumentIterator implements Iterator<CamundaSourceDocument> {

    protected Iterator<TreeEntry> treeEntries;
    protected Repository repository;
    protected RepositoryBranch masterBranch;
    protected Path repositoryPath;
    
    public MarkdownDocumentIterator() {
      RepositoryService repositoryService = new RepositoryService(client);
      try {

        LOG.info("Processing repository " + repositoryOwner + "/" + repositoryName);
        this.repository = repositoryService.getRepository(repositoryOwner, repositoryName);
        
        // clone repository
        this.repositoryPath = Files.createTempDirectory("camunda-docs-rec-github");
        Git
          .cloneRepository()
          .setURI(repository.getCloneUrl())
          .setDirectory(repositoryPath.toFile())
          .call();
        
        // TODO: remove repository once done
        
        // identify master branch
        for (RepositoryBranch branch : repositoryService.getBranches(repository)) {
          if (branch.getName().equals(repository.getMasterBranch())) {
            this.masterBranch = branch;
            break;
          }
        }
        
        if (this.masterBranch == null) {
          throw new RuntimeException("Could not identify master branch");
        }
        
        String latestCommit = this.masterBranch.getCommit().getSha();
        
        // list files on master branch
        DataService dataService = new DataService(client);
        Tree tree = dataService.getTree(repository, latestCommit, true);
        List<TreeEntry> matchingEntries = new ArrayList<TreeEntry>();
        for (TreeEntry entry : tree.getTree()) {
          if (filePattern.matcher(entry.getPath()).matches()) {
            matchingEntries.add(entry);
          }
        }
        
        this.treeEntries = matchingEntries.iterator();
      } catch (Exception e) {
        throw new RuntimeException("Could not access repository " + repositoryOwner + "/" + repositoryName, e);
      }
    }
    
    public boolean hasNext() {
      return treeEntries.hasNext();
    }

    public CamundaSourceDocument next() {
      TreeEntry nextEntry = treeEntries.next();
      File entryFile = repositoryPath.resolve(nextEntry.getPath()).toFile();
      
      GithubMarkdownDocument document = null;
      try {
        FileInputStream inputStream = new FileInputStream(entryFile);
        String content = IOUtils.toString(inputStream);
        inputStream.close();
        
        document = markdownFileAsDocument(repository, masterBranch, repositoryType, nextEntry, content);
      } catch (Exception e) {
        LOG.warn("Could not read file; skipping it", e);
      }
      
      
      return document;
    }

    public void remove() {
      throw new UnsupportedOperationException();
      
    }
    
    protected GithubMarkdownDocument markdownFileAsDocument(Repository repository, RepositoryBranch branch, 
        String repositoryType, TreeEntry treeEntry, String content) {
      String link = repository.getHtmlUrl() + "/tree/" + branch.getName() + "/" + treeEntry.getPath();
      // TODO: extract title from markdown
      PegDownProcessor markdownProcessor = new PegDownProcessor();
      String htmlContent = markdownProcessor.markdownToHtml(content);
      Document parsedContent = Jsoup.parse(htmlContent);
      
      String title = extractTitle(parsedContent, content, treeEntry.getPath());
      String cleanContent = parsedContent.text();
      
      GithubMarkdownDocument document = new GithubMarkdownDocument();
      document.setContent(cleanContent);
      document.setTitle(title);
      document.setLink(link);
      document.setType(repositoryType);
      
      return document;
      
    }
    
    protected String extractTitle(Document parsedHtmlContent, String rawContent, String defaultTitle) {
      
      // use "title" property in YAML header
      Matcher yamlHeaderMatcher = YAML_HEADER_PATTERN.matcher(rawContent);
      if (yamlHeaderMatcher.find()) {
        String headerContent = yamlHeaderMatcher.group(1);
        Yaml yaml = new Yaml();
        Map<String, Object> header = (Map<String, Object>) yaml.load(headerContent);
        Object title = header.get("title");
        if (title != null && title instanceof String) {
          return (String) title;
        }
      }
      
      // use first h1 tag
      Elements h1Elements = parsedHtmlContent.getElementsByTag("h1");
      if (!h1Elements.isEmpty()) {
        return h1Elements.get(0).text();
      }
      
      // use default
      return defaultTitle;
    }
    
  }
  
}
