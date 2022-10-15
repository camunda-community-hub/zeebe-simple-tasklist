package io.zeebe.tasklist.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class NotificationService {

  private final String basePath;

  public NotificationService(@Value("${server.servlet.context-path}") final String basePath) {
    this.basePath = basePath.endsWith("/") ? basePath : basePath + "/";
  }

  @Autowired private SimpMessagingTemplate webSocket;

  public void sendNewTask() {
    final TaskNotification notification = new TaskNotification("new tasks");

    webSocket.convertAndSend(basePath +"notifications/tasks", notification);
  }

  public void sendTaskCanceled() {
    final TaskNotification notification = new TaskNotification("tasks canceled");

    webSocket.convertAndSend(basePath +"notifications/tasks", notification);
  }
}
