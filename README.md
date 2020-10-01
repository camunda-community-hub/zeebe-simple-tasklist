Zeebe Simple Tasklist --
=========================

A [Zeebe](https://zeebe.io) worker to manage manual/user tasks in a workflow. It shows all jobs of type `user` as a task/todo-list. A user can complete the tasks with requested data. 

## Usage --

Example BPMN with service task:
             
 ```xml
 <bpmn:serviceTask id="userTask" name="User Task">
   <bpmn:extensionElements>
     <zeebe:taskDefinition type="user" />
     <zeebe:taskHeaders>
       <zeebe:header key="name" value="My User Task" />
       <zeebe:header key="description" value="My first user task with a form field." />
       <zeebe:header key="formFields" value="[{\"key\":\"orderId\", \"label\":\"Order Id\", \"type\":\"string\"}]" />
       <zeebe:header key="assignee" value="demo" />
     </zeebe:taskHeaders>
   </bpmn:extensionElements>
 </bpmn:serviceTask>
 ```  

* the worker is registered for jobs of type `user`
* optional custom headers:
  * `name` - the name of the task _(default: the element id)_
  * `description` - a description what is the task about
  * `taskForm` (HTML) - the form to show and provide the task data ([example task form](https://github.com/zeebe-io/zeebe-simple-tasklist/blob/master/src/test/resources/custom-task-form.html))
  * `formFields` (JSON) - the form fields for the default task form, if no task form is set
  * `assignee` - the name of the user which should be assigned to the task
  * `candidateGroup` - the name of the group which can claim the task
* optional variables:
  * `assignee` - the name of the user which should be assigned to the task, if not set as header
  * `candidateGroup` - the name of the group which can claim the task, if not set as header
  
### Default Task Form --

If no `taskForm` is defined then the default task form is used. It takes the `formFields` and renders a form with all defined fields. The fields are defined as JSON list, for example:

```
[{
    \"key\":\"orderId\", 
    \"label\":\"Order Id\", 
    \"type\":\"string\"
  }, {
    \"key\":\"price\", 
    \"label\":\"Price\", 
    \"type\":\"number\"
  }
]`)
```

The `type` must be one of: string, number, boolean.

## Install --

### Docker --

The docker image for the worker is published to [DockerHub](https://hub.docker.com/r/camunda/zeebe-simple-tasklist).

```
docker pull camunda/zeebe-simple-tasklist:latest
```

* ensure that a Zeebe broker is running with a Hazelcast exporter (>= 0.8.0-alpha1)  
* forward the Hazelcast port to the docker container (default: `5701`)
* configure the connection to the Zeebe broker by setting `zeebe.client.broker.contactPoint` (default: `localhost:26500`) 
* configure the connection to Hazelcast by setting `zeebe.client.worker.hazelcast.connection` (default: `localhost:5701`) 

For a local setup, the repository contains a [docker-compose file](docker/docker-compose.yml). It starts a Zeebe broker with the Hazelcast exporter and the worker. 

```
mvn clean install -DskipTests
cd docker
docker-compose up
```

### Manual --

1. Download the latest [worker JAR](https://github.com/zeebe-io/zeebe-simple-tasklist/releases) _(zeebe-simple-tasklist-%{VERSION}.jar
)_

1. Start the worker
	`java -jar zeebe-simple-tasklist-{VERSION}.jar`

1. Go to http://localhost:8081

1. Login with `demo/demo`

### Configuration --

The worker is a Spring Boot application that uses the [Spring Zeebe Starter](https://github.com/zeebe-io/spring-zeebe). The configuration can be changed via environment variables or an `application.yaml` file. See also the following resources:
* [Spring Zeebe Configuration](https://github.com/zeebe-io/spring-zeebe#configuring-zeebe-connection)
* [Spring Boot Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config)

By default, the port is set to `8081` and the admin user is created with `demo/demo`.

```
zeebe:
  client:
    worker:
      defaultName: zeebe-simple-tasklist
      defaultType: user
      threads: 2
    
      hazelcast:
        connection: localhost:5701
        connectionTimeout: PT30S
    
      tasklist:
        defaultTaskForm: /templates/default-task-form.html
        adminUsername: demo
        adminPassword: demo

    job.timeout: 2592000000 # 30 days
    broker.contactPoint: 127.0.0.1:26500
    security.plaintext: true

spring:

  datasource:
    url: jdbc:h2:~/zeebe-tasklist
    user: sa
    password:
    driverClassName: org.h2.Driver

  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: update

server:
  port: 8081
```

## Build from Source --

Build with Maven

`mvn clean install`

## Code of Conduct --

This project adheres to the Contributor Covenant [Code of
Conduct](/CODE_OF_CONDUCT.md). By participating, you are expected to uphold
this code. Please report unacceptable behavior to code-of-conduct@zeebe.io.

## License --

[Apache License, Version 2.0](/LICENSE) 

## About --

![screencast](docs/zeebe-simple-tasklist.gif)
