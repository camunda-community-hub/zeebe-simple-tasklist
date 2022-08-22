package io.zeebe.tasklist;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class Demo {

  public static void main(String[] args) {

    final ZeebeClient client = ZeebeClient.newClientBuilder().usePlaintext().build();

    final BpmnModelInstance process =
        Bpmn.createExecutableProcess("demo-process")
            .startEvent()
            .userTask("task-1")
            .userTask(
                "task-2",
                t ->
                    t.zeebeTaskHeader("name", "Task 2")
                        .zeebeTaskHeader("description", "Task with form fields")
                        .zeebeTaskHeader(
                            "formFields",
                            "[{\"key\":\"orderId\", \"label\":\"Order Id\", \"type\":\"string\"}, {\"key\":\"price\", \"label\":\"Price\", \"type\":\"number\"}]"))
            .userTask(
                "task-3",
                t ->
                    t.zeebeTaskHeader("name", "Task 3")
                        .zeebeTaskHeader("description", "Task with custom form")
                        .zeebeTaskHeader("taskForm", getCustomTaskForm()))
            .done();

    client.newDeployResourceCommand().addProcessModel(process, "demoProcess.bpmn").send().join();

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
