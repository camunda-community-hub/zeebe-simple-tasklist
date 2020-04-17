package io.zeebe.tasklist;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import io.zeebe.exporter.proto.Schema;
import io.zeebe.hazelcast.connect.java.ZeebeHazelcast;
import io.zeebe.protocol.record.intent.JobIntent;
import io.zeebe.tasklist.entity.HazelcastConfig;
import io.zeebe.tasklist.repository.HazelcastConfigRepository;
import io.zeebe.tasklist.repository.TaskRepository;
import io.zeebe.tasklist.view.NotificationService;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HazelcastService {

  private static final Logger LOG = LoggerFactory.getLogger(HazelcastService.class);

  @Value("${zeebe.worker.hazelcast.connection}")
  private String hazelcastConnection;

  @Autowired private NotificationService notificationService;
  @Autowired private TaskRepository taskRepository;
  @Autowired private HazelcastConfigRepository hazelcastConfigRepository;

  private ZeebeHazelcast hazelcast;

  @PostConstruct
  public void connect() {

    final var hazelcastConfig =
        hazelcastConfigRepository
            .findById("cfg")
            .orElseGet(
                () -> {
                  final var config = new HazelcastConfig();
                  config.setId("cfg");
                  config.setSequence(-1);
                  return config;
                });

    final ClientConfig clientConfig = new ClientConfig();
    clientConfig.getNetworkConfig().addAddress(hazelcastConnection);

    try {
      LOG.info("Connecting to Hazelcast '{}'", hazelcastConnection);
      final HazelcastInstance hz = HazelcastClient.newHazelcastClient(clientConfig);

      final var builder =
          ZeebeHazelcast.newBuilder(hz)
              .addJobListener(this::handleJob)
              .postProcessListener(
                  sequence -> {
                    hazelcastConfig.setSequence(sequence);
                    hazelcastConfigRepository.save(hazelcastConfig);
                  });

      if (hazelcastConfig.getSequence() >= 0) {
        builder.readFrom(hazelcastConfig.getSequence());
      } else {
        builder.readFromHead();
      }

      hazelcast = builder.build();

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

  @PreDestroy
  public void close() throws Exception {
    if (hazelcast != null) {
      hazelcast.close();
    }
  }
}
