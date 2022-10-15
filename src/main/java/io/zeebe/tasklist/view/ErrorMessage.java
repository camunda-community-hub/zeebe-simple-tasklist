package io.zeebe.tasklist.view;

public class ErrorMessage {

  private String message;

  public ErrorMessage(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(final String message) {
    this.message = message;
  }
}
