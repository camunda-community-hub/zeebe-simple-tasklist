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
package io.zeebe.tasklist.repository;

import io.zeebe.tasklist.entity.TaskEntity;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends PagingAndSortingRepository<TaskEntity, Long> {

  long countByAssignee(String assignee);

  List<TaskEntity> findAllByAssignee(String assignee, Pageable pageable);

  @Query(
      value =
          "SELECT count(t) FROM TASK t WHERE t.assignee = :assignee OR (t.assignee is null AND (t.candidateGroup is null OR t.candidateGroup IN :groups))")
  long countByClaimable(
      @Param("assignee") String assignee, @Param("groups") Collection<String> groups);

  @Query(
      value =
          "SELECT t FROM TASK t WHERE t.assignee = :assignee OR (t.assignee is null AND (t.candidateGroup is null OR t.candidateGroup IN :groups))")
  List<TaskEntity> findAllByClaimable(
      @Param("assignee") String assignee,
      @Param("groups") Collection<String> groups,
      Pageable pageable);
}
