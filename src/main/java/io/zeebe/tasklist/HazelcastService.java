package io.zeebe.tasklist;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import io.zeebe.exporter.proto.Schema;
import io.zeebe.hazelcast.connect.java.ZeebeHazelcast;
import io.zeebe.protocol.record.intent.JobIntent;
import io.zeebe.tasklist.repository.TaskRepository;
import io.zeebe.tasklist.view.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HazelcastService {

  private static final Logger LOG = LoggerFactory.getLogger(HazelcastService.class);

  @Autowired private NotificationService notificationService;

  @Autowired private TaskRepository taskRepository;

  public void connect(String hazelcastConnection, String topic) {

    final ClientConfig clientConfig = new ClientConfig();
    clientConfig.getNetworkConfig().addAddress(hazelcastConnection);

    try {
      LOG.info("Connecting to Hazelcast '{}'", hazelcastConnection);
      final HazelcastInstance hz = HazelcastClient.newHazelcastClient(clientConfig);

      final ZeebeHazelcast zeebeHazelcast = new ZeebeHazelcast(hz);

      LOG.info("Listening on Hazelcast topic '{}' for jobs", topic);
      zeebeHazelcast.addJobListener(topic, this::handleJob);

    } catch (Exception e) {
      LOG.warn("Failed to connect to Hazelcast. Still works but no updates will be received.", e);
    }
  }

  private void handleJob(Schema.JobRecord job) {
    if (isCanceled(job)) {

      taskRepository
          .findById(job.getMetadata().getKey())
          .ifPresent(
              task -> {
                taskRepository.delete(task);

                notificationService.sendTaskCanceled();
              });
    }
  }

  private boolean isCanceled(Schema.JobRecord job) {
    final String intent = job.getMetadata().getIntent();
    return JobIntent.CANCELED.name().equals(intent);
  }
}
