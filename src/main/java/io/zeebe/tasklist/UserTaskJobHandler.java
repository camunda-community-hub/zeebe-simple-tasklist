package io.zeebe.tasklist;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import io.zeebe.tasklist.entity.TaskEntity;
import io.zeebe.tasklist.repository.TaskRepository;
import io.zeebe.tasklist.view.FormField;
import io.zeebe.tasklist.view.NotificationService;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class UserTaskJobHandler implements JobHandler {

  private static final List<String> SUPPORTED_FIELD_TYPES =
      Arrays.asList("string", "number", "boolean");

  private static final TypeReference<List<String>> CANDIDATE_GROUPS_TYPE = new TypeReference<>() {};

  private final TaskDataSerializer serializer = new TaskDataSerializer();

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired private TaskRepository repository;

  @Autowired private NotificationService notificationService;

  @Override
  @ZeebeWorker(timeout = 2592000000L) // 30 days
  public void handle(JobClient client, ActivatedJob job) {

    final TaskEntity entity = new TaskEntity();

    entity.setKey(job.getKey());
    entity.setTimestamp(Instant.now().toEpochMilli());
    entity.setVariables(job.getVariables());

    final Map<String, String> customHeaders = job.getCustomHeaders();
    final Map<String, Object> variables = job.getVariablesAsMap();

    final String name = (String) customHeaders.getOrDefault("name", job.getElementId());
    entity.setName(name);

    final String description = (String) customHeaders.getOrDefault("description", "");
    entity.setDescription(description);

    Optional.ofNullable((String) customHeaders.get("formFields"))
        .ifPresent(
            formFields -> {
              validateFormFields(formFields);
              entity.setFormFields(formFields);
            });

    final String taskForm = (String) customHeaders.get("taskForm");
    entity.setTaskForm(taskForm);

    final String assignee = readAssignee(customHeaders, variables);
    entity.setAssignee(assignee);

    // TODO: handle more than one candidate group
    final List<String> candidateGroups = readCandidateGroups(customHeaders, variables);
    if (candidateGroups.size() >= 1) {
      entity.setCandidateGroup(candidateGroups.get(0));
    }

    repository.save(entity);

    notificationService.sendNewTask();
  }

  private static String readAssignee(
      final Map<String, String> customHeaders, final Map<String, Object> variables) {
    return customHeaders.getOrDefault(
        "io.camunda.zeebe:assignee", (String) variables.get("assignee"));
  }

  private List<String> readCandidateGroups(
      final Map<String, String> customHeaders, final Map<String, Object> variables) {
    final String candidateGroupsAsString =
        customHeaders.getOrDefault(
            "io.camunda.zeebe:candidateGroups", (String) variables.get("candidateGroups"));

    if (candidateGroupsAsString == null || candidateGroupsAsString.isBlank()) {
      return Collections.emptyList();
    }

    try {
      return objectMapper.readValue(candidateGroupsAsString, CANDIDATE_GROUPS_TYPE);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(
          "Fail to parse candidateGroup '%s'".formatted(candidateGroupsAsString));
    }
  }

  private void validateFormFields(String form) {
    final List<FormField> formFields = serializer.readFormFields(form);

    formFields.forEach(
        field -> {
          if (field.getKey() == null) {
            throw new RuntimeException("form-field must have a 'key'");
          }

          final String type = field.getType();
          if (!SUPPORTED_FIELD_TYPES.contains(type)) {
            throw new RuntimeException(
                String.format(
                    "type of form-field '%s' is not supported. Must be one of: %s",
                    type, SUPPORTED_FIELD_TYPES));
          }
        });
  }
}
