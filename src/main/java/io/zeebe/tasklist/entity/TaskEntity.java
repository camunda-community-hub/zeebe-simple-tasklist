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
package io.zeebe.tasklist.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

@Entity(name = "TASK")
public class TaskEntity {

  @Id
  @Column(name = "KEY_")
  private long key;

  @Column(name = "VARIABLES_")
  @Lob
  private String variables;

  @Column(name = "TIMESTAMP_")
  private long timestamp;

  @Column(name = "NAME_")
  private String name;

  @Column(name = "DESCRIPTION_")
  private String description;

  @Column(name = "FORM_FIELDS_")
  private String formFields;

  @Column(name = "TASK_FORM_")
  @Lob
  private String taskForm;

  @Column(name = "ASSIGNEE_")
  private String assignee;

  @Column(name = "CANDIDATE_GROUP_")
  private String candidateGroup;

  public long getKey() {
    return key;
  }

  public void setKey(long key) {
    this.key = key;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public String getVariables() {
    return variables;
  }

  public void setVariables(String variables) {
    this.variables = variables;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getFormFields() {
    return formFields;
  }

  public void setFormFields(String formFields) {
    this.formFields = formFields;
  }

  public String getTaskForm() {
    return taskForm;
  }

  public void setTaskForm(String taskForm) {
    this.taskForm = taskForm;
  }

  public String getAssignee() {
    return assignee;
  }

  public void setAssignee(String assignee) {
    this.assignee = assignee;
  }

  public String getCandidateGroup() {
    return candidateGroup;
  }

  public void setCandidateGroup(String candidateGroup) {
    this.candidateGroup = candidateGroup;
  }
}
