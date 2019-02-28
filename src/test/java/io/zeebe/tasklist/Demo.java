package io.zeebe.tasklist;

import io.zeebe.client.ZeebeClient;
import io.zeebe.model.bpmn.Bpmn;
import io.zeebe.model.bpmn.BpmnModelInstance;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class Demo {

  public static void main(String[] args) {

    final ZeebeClient client = ZeebeClient.newClient();

    final BpmnModelInstance workflow =
        Bpmn.createExecutableProcess("demo-process")
            .startEvent()
            .serviceTask("task-1", t -> t.zeebeTaskType("user"))
            .serviceTask(
                "task-2",
                t ->
                    t.zeebeTaskType("user")
                        .zeebeTaskHeader("name", "Task 2")
                        .zeebeTaskHeader("description", "Task with form fields")
                        .zeebeTaskHeader(
                            "formFields",
                            "[{\"key\":\"orderId\", \"label\":\"Order Id\", \"type\":\"string\"}, {\"key\":\"price\", \"label\":\"Price\", \"type\":\"number\"}]"))
            .serviceTask(
                "task-3",
                t ->
                    t.zeebeTaskType("user")
                        .zeebeTaskHeader("name", "Task 3")
                        .zeebeTaskHeader("description", "Task with custom form")
                        .zeebeTaskHeader("taskForm", getCustomTaskForm()))
            .done();

    client.newDeployCommand().addWorkflowModel(workflow, "demoProcess.bpmn").send().join();

    IntStream.range(0, 3)
        .forEach(
            i -> {
              final Map<String, Object> variables = new HashMap<>();
              variables.put("task-nr", i);
              // variables.put("assignee", "user1");
              // variables.put("candidateGroup", "group1");

              client
                  .newCreateInstanceCommand()
                  .bpmnProcessId("demo-process")
                  .latestVersion()
                  .variables(variables)
                  .send()
                  .join();
            });
  }

  private static String getCustomTaskForm() {
    try {
      return new String(
          Files.readAllBytes(Paths.get(Demo.class.getResource("/custom-task-form.html").toURI())));
    } catch (IOException | URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
}
