package io.zeebe.tasklist.view;

public class TaskNotification {

  private String message;

  public TaskNotification(String message) {
    this.setMessage(message);
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

}
