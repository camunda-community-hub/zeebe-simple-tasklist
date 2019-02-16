package io.zeebe.tasklist.view;

public class TaskNotification {

  private String message;
  private long taskCount;

  public TaskNotification(String message, long taskCount) {
    this.setMessage(message);
    this.setTaskCount(taskCount);
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public long getTaskCount() {
    return taskCount;
  }

  public void setTaskCount(long taskCount) {
    this.taskCount = taskCount;
  }
}
