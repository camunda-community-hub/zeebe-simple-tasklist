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
package io.zeebe.tasklist;

import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableSpringDataWebSupport
public class ZeebeSimpleTasklistApp {

  private static final Logger LOG = LoggerFactory.getLogger(ZeebeSimpleTasklistApp.class);

  @Value("${io.zeebe.tasklist.connectionString}")
  private String connectionString;

  @Value("${io.zeebe.tasklist.adminUsername}")
  private String adminUsername;

  @Value("${io.zeebe.tasklist.adminPassword}")
  private String adminPassword;

  @Value("${io.zeebe.tasklist.hazelcast.connection}")
  private String hazelcastConnection;

  @Value("${io.zeebe.tasklist.hazelcast.topic}")
  private String hazelcastTopic;

  @Autowired private ZeebeClientService zeebeClientService;

  @Autowired private HazelcastService hazelcastService;

  @Autowired private UserService userService;

  public static void main(String... args) {
    SpringApplication.run(ZeebeSimpleTasklistApp.class, args);
  }

  @PostConstruct
  public void init() {
    LOG.info("Connecting to Zeebe broker '{}'", connectionString);
    zeebeClientService.connect(connectionString);

    hazelcastService.connect(hazelcastConnection, hazelcastTopic);

    if (!adminUsername.isEmpty() && !userService.hasUserWithName(adminUsername)) {
      LOG.info(
          "Creating admin user with name '{}' and password '{}'", adminUsername, adminPassword);
      userService.newAdminUser(adminUsername, adminPassword);
    }
  }
}
