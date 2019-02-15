package io.zeebe.tasklist.view;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import io.zeebe.tasklist.TaskDataSerializer;
import io.zeebe.tasklist.entity.TaskEntity;
import io.zeebe.tasklist.repository.TaskRepository;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ViewController {

  private final TaskDataSerializer serializer = new TaskDataSerializer();

  @Value("${io.zeebe.tasklist.defaultTaskForm}")
  private String defaultTaskForm;

  @Autowired private TaskRepository repository;

  private Template taskTemplate;

  @PostConstruct
  public void loadTemplate() {
    try {
      final InputStream inputStream = getClass().getResourceAsStream(defaultTaskForm);

      final Reader reader;
      if (inputStream != null) {
        reader = new InputStreamReader(inputStream);
      } else {
        reader = Files.newBufferedReader(Paths.get(defaultTaskForm));
      }

      taskTemplate = Mustache.compiler().compile(reader);
    } catch (Exception e) {
      throw new RuntimeException("Failed to load default task templete", e);
    }
  }

  @GetMapping("/")
  public String index(Map<String, Object> model, @PageableDefault(size = 10) Pageable pageable) {
    return taskList(model, pageable);
  }

  @GetMapping("/views/tasks")
  public String taskList(Map<String, Object> model, @PageableDefault(size = 10) Pageable pageable) {

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
      @PathVariable("key") long key,
      Map<String, Object> model,
      @PageableDefault(size = 10) Pageable pageable) {

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

                final String taskForm = taskTemplate.execute(templateData);
                model.put("taskForm", taskForm);

              } catch (Exception e) {
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
    model.put("currentPage", currentPage);
    model.put("page", currentPage + 1);
    if (currentPage > 0) {
      model.put("prevPage", currentPage - 1);
    }
    if (count > (1 + currentPage) * pageable.getPageSize()) {
      model.put("nextPage", currentPage + 1);
    }
  }
}
