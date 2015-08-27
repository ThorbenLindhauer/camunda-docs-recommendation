package org.camunda.bpm.hackdays.recommendation.clustering;

import java.io.Serializable;

import org.jsoup.Jsoup;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;

public class RemoveHtmlTags extends Pipe implements Serializable {

  private static final long serialVersionUID = 1L;

  public Instance pipe (Instance carrier) {

    if (carrier.getData() instanceof String) {
      String data = (String) carrier.getData();
      String cleanedText = Jsoup.parse(data).text();
      carrier.setData(cleanedText);
    }
    else {
      throw new IllegalArgumentException("CharSequenceLowercase expects a String, found a " + carrier.getData().getClass());
    }

    return carrier;
  }
}
