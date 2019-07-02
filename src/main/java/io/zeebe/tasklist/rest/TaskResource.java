/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zeebe.tasklist.rest;

import io.zeebe.tasklist.ZeebeClientService;
import io.zeebe.tasklist.entity.TaskEntity;
import io.zeebe.tasklist.repository.TaskRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskResource {

  @Autowired private TaskRepository repository;

  @Autowired private ZeebeClientService zeebeClientService;

  @RequestMapping(path = "/{key}/complete", method = RequestMethod.PUT)
  public void completeTask(
      @PathVariable("key") long key, @RequestBody List<TaskVariable> variables) {

    final TaskEntity task =
        repository
            .findById(key)
            .orElseThrow(() -> new RuntimeException("No task found with key: " + key));

    final Map<String, Object> result =
        variables.stream().collect(Collectors.toMap(TaskVariable::getKey, TaskVariable::getValue));

    zeebeClientService.getClient().newCompleteCommand(key).variables(result).send().join();

    repository.delete(task);
  }

  @RequestMapping(path = "/{key}/claim", method = RequestMethod.PUT)
  public void claimTask(@PathVariable("key") long key) {

    final TaskEntity task =
        repository
            .findById(key)
            .orElseThrow(() -> new RuntimeException("No task found with key: " + key));

    final String username = SecurityContextHolder.getContext().getAuthentication().getName();
    task.setAssignee(username);

    repository.save(task);
  }
}
