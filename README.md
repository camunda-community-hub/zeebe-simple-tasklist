Zeebe Simple Tasklist
=========================

[![](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)
[![](https://img.shields.io/badge/Lifecycle-Incubating-blue)](https://github.com/Camunda-Community-Hub/community/blob/main/extension-lifecycle.md#incubating-)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

[![Compatible with: Camunda Platform 8](https://img.shields.io/badge/Compatible%20with-Camunda%20Platform%208-0072Ce)](https://github.com/camunda-community-hub/community/blob/main/extension-lifecycle.md#compatiblilty)
[![](https://img.shields.io/badge/Maintainer%20Wanted-This%20extension%20is%20in%20search%20of%20a%20Maintainer-ff69b4)](https://github.com/camunda-community-hub/community/blob/main/extension-lifecycle.md)

https://docs.camunda.io/docs/components/tasklist/introduction-to-tasklist/

> :warning: **This is not related to the official Tasklist component in Camunda 8**. The documentation on Tasklist can be found [here.](https://docs.camunda.io/docs/components/tasklist/introduction-to-tasklist/) We do not recommend using this extension instead because it is currently not maintained and older.

A [Zeebe](https://zeebe.io) worker to manage user tasks in a workflow. It shows all jobs of
the user task's type as a task/todo-list. A user can complete the tasks with requested data.

## Usage

Example BPMN with a user task:

 ```xml

<bpmn:userTask id="userTask" name="User Task">
  <bpmn:extensionElements>
    <zeebe:taskHeaders>
      <zeebe:header key="name" value="My User Task"/>
      <zeebe:header key="description" value="My first user task with a form field."/>
      <zeebe:header key="formFields" value="[{\" key\":\"orderId\", \"label\":\"Order Id\",
      \"type\":\"string\"}]" />
      <zeebe:assignmentDefinition assignee="demo" />
    </zeebe:taskHeaders>
  </bpmn:extensionElements>
</bpmn:userTask>
 ```  

* the worker is registered for jobs of type `io.camunda.zeebe:userTask` (the reserved job type of user tasks)
* optional custom headers:
    * `name` - the name of the task _(default: the element id)_
    * `description` - a description what is the task about
    * `taskForm` (HTML) - the form to show and provide the task
      data ([example task form](https://github.com/zeebe-io/zeebe-simple-tasklist/blob/master/src/test/resources/custom-task-form.html))
    * `formFields` (JSON) - the form fields for the default task form, if no task form is set

### Default Task Form

If no `taskForm` is defined then the default task form is used. It takes the `formFields` and
renders a form with all defined fields. The fields are defined as JSON list, for example:

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

## Install

### Docker

The docker image for the worker is published
to [GitHub Packages](https://github.com/orgs/camunda-community-hub/packages/container/package/zeebe-simple-tasklist)
.

```
docker pull ghcr.io/camunda-community-hub/zeebe-simple-tasklist:latest
```

* ensure that a Zeebe broker is running with
  a [Hazelcast exporter](https://github.com/camunda-community-hub/zeebe-hazelcast-exporter#install) (>
  = `1.0.0`)
* forward the Hazelcast port to the docker container (default: `5701`)
* configure the connection to the Zeebe broker by setting `zeebe.client.broker.gateway-address` (
  default: `localhost:26500`)
* configure the connection to Hazelcast by setting `zeebe.client.worker.hazelcast.connection` (
  default: `localhost:5701`)

If the Zeebe broker runs on your local machine with the default configs then start the container
with the following command:

```
docker run --network="host" ghcr.io/camunda-community-hub/zeebe-simple-tasklist:latest
```

For a local setup, the repository contains a [docker-compose file](docker/docker-compose.yml). It
starts a Zeebe broker with the Hazelcast exporter and the application.

```
cd docker
docker-compose --profile in-memory up
```

Go to http://localhost:8081

To use PostgreSQL instead of the in-memory database, use the profile `postgres`.

```
docker-compose --profile postgres up
```

### Manual

1. Download the
   latest [application JAR](https://github.com/zeebe-io/zeebe-simple-tasklist/releases) _(
   zeebe-simple-tasklist-%{VERSION}.jar
   )_

1. Start the application
   `java -jar zeebe-simple-tasklist-{VERSION}.jar`

1. Go to http://localhost:8081

1. Login with `demo/demo`

### Configuration

The worker is a Spring Boot application that uses
the [Spring Zeebe Starter](https://github.com/zeebe-io/spring-zeebe). The configuration can be
changed via environment variables or an `application.yaml` file. See also the following resources:

* [Spring Zeebe Configuration](https://github.com/zeebe-io/spring-zeebe#configuring-zeebe-connection)
* [Spring Boot Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config)

By default, the port is set to `8081`, the admin user is created with `demo/demo`, and the database
is only in-memory (i.e. not persistent).

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
  servlet:
    context-path: /  
  allowedOriginsUrls: ""
```

#### Change the Context-Path

The context-path or base-path of the application can be changed using the following property:

``` 
server:
  servlet:
    context-path: /tasklist/
```

It is then available under http://localhost:8081/tasklist.

#### Customize the Look & Feel

You can customize the look & feel of the Zeebe Simple Tasklist (aka. white-labeling). For example, to change the logo or
alter the background color. The following configurations are available:

```
- white-label.logo.path=img/logo.png
- white-label.custom.title=Zeebe Simple Tasklist
- white-label.custom.css.path=css/custom.css
- white-label.custom.js.path=js/custom.js
```

#### Change the Database

For example, using PostgreSQL:

* change the following database configuration settings

```
- spring.datasource.url=jdbc:postgresql://db:5432/postgres
- spring.datasource.username=postgres
- spring.datasource.password=zeebe
- spring.datasource.driverClassName=org.postgresql.Driver
- spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

* the PostgreSQL database driver is already bundled

See the [docker-compose file](docker/docker-compose.yml) (profile: `postgres`) for a sample
configuration with PostgreSQL.

#### Cross Origin Requests

To enable Simple Tasklist to send CORS header with every HTTP response,
add the allowed origins (`;` separated) in the following property:

``` 
server:
  allowedOriginsUrls: http://localhost:8081;https://tasklist.cloud-provider.io:8081
```

This will then set ```Access-Control-Allow-Origin``` headers in every HTTP response.

## Build from Source

Build with Maven

`mvn clean install`

## Code of Conduct

This project adheres to the Contributor Covenant [Code of
Conduct](/CODE_OF_CONDUCT.md). By participating, you are expected to uphold
this code. Please report unacceptable behavior to code-of-conduct@zeebe.io.

## License

[Apache License, Version 2.0](/LICENSE)

## About

![screencast](docs/zeebe-simple-tasklist.gif)
