package io.zeebe.tasklist;

import io.zeebe.client.ZeebeClient;
import io.zeebe.model.bpmn.Bpmn;
import io.zeebe.model.bpmn.BpmnModelInstance;
import java.util.Collections;
import java.util.stream.IntStream;

public class Demo {

  public static void main(String[] args) {

    final ZeebeClient client = ZeebeClient.newClient();

    final BpmnModelInstance workflow =
        Bpmn.createExecutableProcess("demo-process")
            .startEvent()
            .serviceTask("task_default", t -> t.zeebeTaskType("user"))
            .serviceTask(
                "task_no_form",
                t ->
                    t.zeebeTaskType("user")
                        .zeebeTaskHeader("name", "Task 2")
                        .zeebeTaskHeader("description", "Task without form."))
            .serviceTask(
                "task_string_field",
                t ->
                    t.zeebeTaskType("user")
                        .zeebeTaskHeader("name", "Task 3")
                        .zeebeTaskHeader("description", "Task with string field")
                        .zeebeTaskHeader(
                            "formData",
                            "[{\"key\":\"x\", \"label\":\"any string\", \"type\":\"string\"}]"))
            .serviceTask(
                "task_number_field",
                t ->
                    t.zeebeTaskType("user")
                        .zeebeTaskHeader("name", "Task 4")
                        .zeebeTaskHeader("description", "Task with number field")
                        .zeebeTaskHeader(
                            "formData",
                            "[{\"key\":\"y\", \"label\":\"any number\", \"type\":\"number\"}]"))
            .serviceTask(
                "task_boolean_field",
                t ->
                    t.zeebeTaskType("user")
                        .zeebeTaskHeader("name", "Task 5")
                        .zeebeTaskHeader("description", "Task with boolean field")
                        .zeebeTaskHeader(
                            "formData",
                            "[{\"key\":\"z\", \"label\":\"any boolean\", \"type\":\"boolean\"}]"))
            .serviceTask(
                "task_multiple_field",
                t ->
                    t.zeebeTaskType("user")
                        .zeebeTaskHeader("name", "Task 6")
                        .zeebeTaskHeader("description", "Task with mulitple fields")
                        .zeebeTaskHeader(
                            "formData",
                            "[{\"key\":\"i1\", \"label\":\"any string\", \"type\":\"string\"}, {\"key\":\"i2\", \"label\":\"any number\", \"type\":\"boolean\"}]"))
            .done();

    client
        .workflowClient()
        .newDeployCommand()
        .addWorkflowModel(workflow, "demoProcess.bpmn")
        .send()
        .join();

    IntStream.range(0, 30)
        .forEach(
            i -> {
              client
                  .workflowClient()
                  .newCreateInstanceCommand()
                  .bpmnProcessId("demo-process")
                  .latestVersion()
                  .payload(Collections.singletonMap("a", i))
                  .send()
                  .join();
            });
  }
}
