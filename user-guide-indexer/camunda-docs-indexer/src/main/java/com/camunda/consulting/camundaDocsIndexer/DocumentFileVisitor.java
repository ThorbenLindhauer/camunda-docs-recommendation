package com.camunda.consulting.camundaDocsIndexer;

import static org.camunda.spin.Spin.JSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.markdown4j.Markdown4jProcessor;

public class DocumentFileVisitor extends SimpleFileVisitor<Path> {
  
  private Client elasticsearchClient;
  
  private Pattern findHeader = Pattern.compile("(?s)---\\s(.*)---\\s(.*)");
  private Pattern titlePattern = Pattern.compile("title: ['\"](.*)['\"]");
  private Matcher matcher;
  
  public DocumentFileVisitor(Client elasticsearchClient) {
    this.elasticsearchClient = elasticsearchClient;
  }

  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
    if (file.getFileName().toString().endsWith(".md")) {
      System.out.println("" + file);
      BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset());
      indexDocument(reader, file);
      System.out.println();
    }
    return FileVisitResult.CONTINUE;
  }
  
  protected void indexDocument(BufferedReader reader, Path path) throws IOException {
    String header = "";
    String title = "";
    String baseUrl = "";
    String contentUrl = "";
    String article = "";
    StringBuilder content = new StringBuilder();
    String line = null;
    while ((line = reader.readLine()) != null) {
      content.append(line).append("\n");
    }
    
    String fileContent = content.toString();
    matcher = findHeader.matcher(fileContent);

    while (matcher.find()) {
      header = matcher.group(1);
      article = matcher.group(2);
    }
    
    matcher = titlePattern.matcher(header);
    while (matcher.find()) {
      title = matcher.group(1);
    }

    
    Document document = new Document(title, baseUrl, contentUrl, new Markdown4jProcessor().process(article));
    System.out.println(document.toString());
    String json = JSON(document).toString();
    IndexResponse response = elasticsearchClient.prepareIndex("user-guide", "chapter")
        .setSource(json)
        .execute()
        .actionGet();
  }

  
}
