package io.zeebe.tasklist.view;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import io.zeebe.tasklist.TaskDataSerializer;
import io.zeebe.tasklist.entity.TaskEntity;
import io.zeebe.tasklist.repository.TaskRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ViewController {

  private final TaskDataSerializer serializer = new TaskDataSerializer();

  @Autowired private TaskRepository repository;

  @GetMapping("/")
  public String index(Map<String, Object> model, Pageable pageable) {
    return taskList(model, pageable);
  }

  @GetMapping("/views/tasks")
  public String taskList(Map<String, Object> model, Pageable pageable) {

    final long count = repository.count();

    final List<TaskDto> tasks = new ArrayList<>();
    for (TaskEntity job : repository.findAll(pageable)) {
      final TaskDto dto = toDto(job);
      tasks.add(dto);
    }

    model.put("tasks", tasks);
    model.put("count", count);

    addPaginationToModel(model, pageable, count);

    return "task-list-view";
  }

  @GetMapping("/views/tasks/{key}")
  public String taskList(
      @PathVariable("key") long key, Map<String, Object> model, Pageable pageable) {

    final long count = repository.count();

    final List<TaskDto> tasks = new ArrayList<>();
    for (TaskEntity job : repository.findAll(pageable)) {
      final TaskDto dto = toDto(job);
      dto.setActive(job.getKey() == key);

      tasks.add(dto);
    }

    repository
        .findById(key)
        .ifPresent(
            task -> {
              try {
                final URI uri = getClass().getResource("/default.html").toURI();
                final Path path = Paths.get(uri);

                final BufferedReader reader = Files.newBufferedReader(path);
                final Template tmpl = Mustache.compiler().compile(reader);

                final Map<String, Object> templateData = new HashMap<>();

                final Map<String, Object> taskPayload = serializer.readVariables(task.getPayload());
                templateData.put("variables", taskPayload.entrySet());

                Optional.ofNullable(task.getFormData())
                    .ifPresent(
                        formData -> {
                          final List<FormField> formFields = serializer.readFormFields(formData);
                          formFields.forEach(this::setInputTypeOfFormField);

                          templateData.put("formFields", formFields);
                        });

                final String taskForm = tmpl.execute(templateData);
                model.put("taskForm", taskForm);

              } catch (IOException | URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }

              model.put("task", toDto(task));
            });

    model.put("tasks", tasks);
    model.put("count", count);

    addPaginationToModel(model, pageable, count);

    return "task-list-view";
  }

  private void setInputTypeOfFormField(FormField field) {
    switch (field.getType()) {
      case "string":
        field.setType("text");
        break;
      case "number":
        field.setType("number");
        break;
      case "boolean":
        field.setType("checkbox");
        break;
      default:
        field.setType("text");
        break;
    }
  }

  private TaskDto toDto(TaskEntity entity) {
    final TaskDto dto = new TaskDto();

    dto.setKey(entity.getKey());
    dto.setName(entity.getName());
    dto.setDescription(entity.getDescription());

    final Instant created = Instant.ofEpochMilli(entity.getTimestamp());
    final Duration duration = Duration.between(created, Instant.now());

    if (duration.toDays() > 0) {
      dto.setCreated(duration.toDays() + " days");
    } else if (duration.toHours() > 0) {
      dto.setCreated(duration.toHours() + " hours");
    } else if (duration.toMinutes() > 0) {
      dto.setCreated(duration.toMinutes() + " minutes");
    } else {
      dto.setCreated("few seconds");
    }

    return dto;
  }

  private void addPaginationToModel(
      Map<String, Object> model, Pageable pageable, final long count) {

    final int currentPage = pageable.getPageNumber();
    model.put("page", currentPage + 1);
    if (currentPage > 0) {
      model.put("prevPage", currentPage - 1);
    }
    if (count > (1 + currentPage) * pageable.getPageSize()) {
      model.put("nextPage", currentPage + 1);
    }
  }
}
