package io.zeebe.tasklist;

import io.zeebe.client.api.clients.JobClient;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.response.JobHeaders;
import io.zeebe.client.api.subscription.JobHandler;
import io.zeebe.tasklist.entity.TaskEntity;
import io.zeebe.tasklist.repository.TaskRepository;
import java.time.Instant;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserTaskJobHandler implements JobHandler {

  @Autowired private TaskRepository repository;

  @Override
  public void handle(JobClient client, ActivatedJob job) {

    final TaskEntity entity = new TaskEntity();
    entity.setKey(job.getKey());
    entity.setTimestamp(Instant.now().toEpochMilli());
    entity.setPayload(job.getPayload());

    final JobHeaders headers = job.getHeaders();
    final Map<String, Object> customHeaders = job.getCustomHeaders();

    final String name = (String) customHeaders.getOrDefault("name", headers.getElementId());
    entity.setName(name);

    final String formKey = (String) customHeaders.get("form-key");
    entity.setFormKey(formKey);

    final String assigne = (String) customHeaders.get("assigne");
    entity.setAssigne(assigne);

    repository.save(entity);
  }
}
