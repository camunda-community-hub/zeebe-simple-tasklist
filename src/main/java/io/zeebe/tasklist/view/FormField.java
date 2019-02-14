package io.zeebe.tasklist.view;

import java.util.Optional;

public class FormField {

  private String id;
  private String label;
  private String type = "text";

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getLabel() {
    return Optional.ofNullable(label).orElse(id);
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
