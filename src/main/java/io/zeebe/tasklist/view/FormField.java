package io.zeebe.tasklist.view;

import java.util.Optional;

public class FormField {

  private String key;
  private String label;
  private String type = "text";

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getLabel() {
    return Optional.ofNullable(label).orElse(key);
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
