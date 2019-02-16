package io.zeebe.tasklist.view;

import io.zeebe.tasklist.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class NotificationService {

  @Autowired private SimpMessagingTemplate webSocket;

  @Autowired private TaskRepository taskRepository;

  public void sendNewTask() {
    final TaskNotification notification = new TaskNotification("new tasks", taskRepository.count());

    webSocket.convertAndSend("/notifications/tasks", notification);
  }
}
