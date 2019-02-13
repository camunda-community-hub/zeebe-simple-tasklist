package io.zeebe.tasklist;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import io.zeebe.tasklist.entity.TaskEntity;
import io.zeebe.tasklist.repository.TaskRepository;
import io.zeebe.tasklist.view.TaskDto;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ViewController {

  @Autowired private TaskRepository repository;

  @GetMapping("/")
  public String index(Map<String, Object> model, Pageable pageable) {
    return taskList(model, pageable);
  }

  @GetMapping("/views/tasks")
  public String taskList(Map<String, Object> model, Pageable pageable) {

    final long count = repository.count();

    final List<Object> tasks = new ArrayList<>();
    for (TaskEntity job : repository.findAll(pageable)) {
      tasks.add(job);
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
              //              final String formKey = ;
              //                  Optional.ofNullable(task.getFormKey()).orElse("default.html");

              final ObjectMapper objectMapper = new ObjectMapper();

              try {
                final URI uri = getClass().getResource("/default.html").toURI();
                final Path path = Paths.get(uri);

                final BufferedReader reader = Files.newBufferedReader(path);
                final Template tmpl = Mustache.compiler().compile(reader);

                final Map taskPayload = objectMapper.readValue(task.getPayload(), Map.class);

                final Map<String, Object> templateData = new HashMap<>();
                templateData.putAll(taskPayload);
                templateData.put("taskData", taskPayload.entrySet());

                final String form = tmpl.execute(templateData);
                model.put("taskForm", form);

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

  private TaskDto toDto(TaskEntity entity) {
    final TaskDto dto = new TaskDto();

    dto.setKey(entity.getKey());
    dto.setName(entity.getName());
    dto.setDescription("foobar"); // TODO description

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
