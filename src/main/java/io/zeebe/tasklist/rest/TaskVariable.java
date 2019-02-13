package io.zeebe.tasklist.rest;

public class TaskVariable {

  private String key;
  private Object value;

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }
}
