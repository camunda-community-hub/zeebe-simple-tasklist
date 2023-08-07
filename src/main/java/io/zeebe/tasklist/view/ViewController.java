package io.zeebe.tasklist.view;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import io.zeebe.tasklist.TaskDataSerializer;
import io.zeebe.tasklist.entity.GroupEntity;
import io.zeebe.tasklist.entity.TaskEntity;
import io.zeebe.tasklist.entity.UserEntity;
import io.zeebe.tasklist.repository.GroupRepository;
import io.zeebe.tasklist.repository.TaskRepository;
import io.zeebe.tasklist.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.view.RedirectView;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@Transactional
public class ViewController extends AbstractViewController {

  private final TaskDataSerializer serializer = new TaskDataSerializer();

  @Value("${zeebe.client.worker.tasklist.defaultTaskForm}")
  private String defaultTaskForm;

  @Autowired private TaskRepository taskRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private GroupRepository groupRepository;

  private Template defaultTaskTemplate;

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

      defaultTaskTemplate = Mustache.compiler().compile(reader);
    } catch (Exception e) {
      throw new RuntimeException("Failed to load default task templete", e);
    }
  }

  @GetMapping("/")
  public RedirectView index() {
    return new RedirectView("./views/all-tasks/");
  }

  @GetMapping("/views/my-tasks")
  public String taskList(Map<String, Object> model, @PageableDefault(size = 10) Pageable pageable) {

    final String username = getUsername();
    final long count = taskRepository.countByAssignee(username);

    final List<TaskDto> tasks =
        taskRepository.findAllByAssignee(username, pageable).stream()
            .map(this::toDto)
            .collect(Collectors.toList());

    model.put("tasks", tasks);
    model.put("count", count);

    addPaginationToModel(model, pageable, count);
    addDefaultAttributesToModel(model);
    addCommonsToModel(model);

    return "task-list-view";
  }

  @GetMapping("/views/my-tasks/{key}")
  public String taskList(
      @PathVariable("key") long key,
      Map<String, Object> model,
      @PageableDefault(size = 10) Pageable pageable) {

    final String username = getUsername();

    final long count = taskRepository.countByAssignee(username);

    final List<TaskDto> tasks =
        taskRepository.findAllByAssignee(username, pageable).stream()
            .map(this::toDto)
            .collect(Collectors.toList());

    tasks.stream().forEach(task -> task.setActive(task.getKey() == key));

    taskRepository
        .findById(key)
        .ifPresent(
            task -> {
              final String taskForm = renderTaskForm(task);
              model.put("taskForm", taskForm);

              model.put("task", toDto(task));
            });

    model.put("tasks", tasks);
    model.put("count", count);

    addPaginationToModel(model, pageable, count);
    addDefaultAttributesToModel(model);
    addCommonsToModel(model);

    return "task-list-view";
  }

  private String renderTaskForm(TaskEntity task) {
    try {
      final Map<String, Object> taskPayload = serializer.readVariables(task.getVariables());

      final Template taskTemplate =
          Optional.ofNullable(task.getTaskForm())
              .map(Mustache.compiler()::compile)
              .orElse(defaultTaskTemplate);

      final Map<String, Object> templateData = new HashMap<>();

      final String taskForm = task.getTaskForm();
      if (taskForm != null) {
        templateData.putAll(taskPayload);

      } else {
        templateData.put("variables", taskPayload.entrySet());

        Optional.ofNullable(task.getFormFields())
            .ifPresent(
                form -> {
                  final List<FormField> formFields = serializer.readFormFields(form);
                  formFields.forEach(this::setInputTypeOfFormField);

                  templateData.put("formFields", formFields);
                });
      }

      return taskTemplate.execute(templateData);

    } catch (Exception e) {
      e.printStackTrace();

      return "⚠ Failure while rendering task form.";
    }
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

    Optional.ofNullable(entity.getAssignee())
        .filter(getUsername()::equals)
        .ifPresent(assignee -> dto.setAssigned(true));

    return dto;
  }

  @GetMapping("/views/all-tasks")
  public String allTaskList(
      Map<String, Object> model, @PageableDefault(size = 10) Pageable pageable) {

    final String username = getUsername();
    final List<String> groups = getUserGroupNames();
    final long count = taskRepository.countByClaimable(username, groups);

    final List<TaskDto> tasks =
        taskRepository.findAllByClaimable(username, groups, pageable).stream()
            .map(this::toDto)
            .collect(Collectors.toList());

    model.put("tasks", tasks);
    model.put("count", count);

    addPaginationToModel(model, pageable, count);
    addDefaultAttributesToModel(model);
    addCommonsToModel(model);

    return "task-list-view";
  }

  @GetMapping("/views/all-tasks/{key}")
  public String allTaskList(
      @PathVariable("key") long key,
      Map<String, Object> model,
      @PageableDefault(size = 10) Pageable pageable) {

    final String username = getUsername();
    final List<String> groups = getUserGroupNames();
    final long count = taskRepository.countByClaimable(username, groups);

    final List<TaskDto> tasks =
        taskRepository.findAllByClaimable(username, groups, pageable).stream()
            .map(this::toDto)
            .collect(Collectors.toList());

    tasks.stream().forEach(task -> task.setActive(task.getKey() == key));

    taskRepository
        .findById(key)
        .ifPresent(
            task -> {
              final String taskForm = renderTaskForm(task);
              model.put("taskForm", taskForm);

              model.put("task", toDto(task));
            });

    model.put("tasks", tasks);
    model.put("count", count);

    addPaginationToModel(model, pageable, count);
    addDefaultAttributesToModel(model);
    addCommonsToModel(model);

    return "task-list-view";
  }

  private List<String> getUserGroupNames() {
    return userRepository
        .findById(getUsername())
        .map(
            user ->
                user.getGroups().stream().map(GroupEntity::getName).collect(Collectors.toList()))
        .orElse(Collections.emptyList());
  }

  @GetMapping("/views/users")
  public String userList(Map<String, Object> model, @PageableDefault(size = 10) Pageable pageable) {

    final long count = userRepository.count();

    final List<UserEntity> users = new ArrayList<>();
    for (UserEntity user : userRepository.findAll(pageable)) {
      users.add(user);
    }

    final List<String> groupNames = new ArrayList<>();
    for (GroupEntity group : groupRepository.findAll()) {
      groupNames.add(group.getName());
    }

    model.put("users", users);
    model.put("count", count);
    model.put("availableGroups", groupNames);

    addPaginationToModel(model, pageable, count);
    addDefaultAttributesToModel(model);
    addCommonsToModel(model);

    return "user-view";
  }

  @GetMapping("/views/groups")
  public String groupList(
      Map<String, Object> model, @PageableDefault(size = 10) Pageable pageable) {

    final long count = groupRepository.count();

    final List<GroupEntity> groups = new ArrayList<>();
    for (GroupEntity user : groupRepository.findAll(pageable)) {
      groups.add(user);
    }

    model.put("groups", groups);
    model.put("count", count);

    addPaginationToModel(model, pageable, count);
    addDefaultAttributesToModel(model);
    addCommonsToModel(model);

    return "group-view";
  }

  private String getUsername() {
    final String username = SecurityContextHolder.getContext().getAuthentication().getName();
    return username;
  }
}
