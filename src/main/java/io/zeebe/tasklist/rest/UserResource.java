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

import io.zeebe.tasklist.UserService;
import io.zeebe.tasklist.entity.GroupEntity;
import io.zeebe.tasklist.entity.UserEntity;
import io.zeebe.tasklist.repository.GroupRepository;
import io.zeebe.tasklist.repository.TaskRepository;
import io.zeebe.tasklist.repository.UserRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserResource {

  @Autowired private UserService service;
  @Autowired private UserRepository userRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private TaskRepository taskRepository;

  @RequestMapping(path = "/", method = RequestMethod.POST)
  public void createUser(@RequestBody UserDetailsDto user) {

    if (service.hasUserWithName(user.getUsername())) {
      throw new RuntimeException(
          String.format("User with name '%s' already exists.", user.getUsername()));
    }

    service.newUser(user.getUsername(), user.getPassword());
  }

  @RequestMapping(path = "/{username}", method = RequestMethod.DELETE)
  public void deleteUser(@PathVariable("username") String username) {

    if (!userRepository.existsById(username)) {
      throw new RuntimeException(String.format("User with name '%s' doesn't exist.", username));
    }

    taskRepository
        .findAllByAssignee(username, Pageable.unpaged())
        .forEach(
            task -> {
              task.setAssignee(null);

              taskRepository.save(task);
            });

    userRepository.deleteById(username);
  }

  @RequestMapping(path = "/{username}/group-memberships", method = RequestMethod.PUT)
  public void updateGroupMemberships(
      @PathVariable("username") String username, @RequestBody List<String> groupNames) {

    final UserEntity user =
        userRepository
            .findById(username)
            .orElseThrow(
                () ->
                    new RuntimeException(
                        String.format("User with name '%s' doesn't exist.", username)));

    final Set<GroupEntity> groups = new HashSet<>();
    groupRepository
        .findAllById(groupNames)
        .forEach(
            group -> {
              groups.add(group);
            });
    user.setGroups(groups);

    userRepository.save(user);
  }
}
