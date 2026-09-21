# vmis-app-base

- [About](#about)
- [Development-Requirements](#development-requirements)
- [Runtime-Requirements](#runtime-requirements)
- [Usage](#usage)
- [Features](#features)

## About

The vmis2-app-base is a library for traffic control applications containing several helper classes.

## Development-Requirements

- Java 17 or later
- Maven 3.6 or later
- Spring Boot 3.x

## Runtime-Requirements

- Java 17
- Spring Boot 3.x

## Usage

Add the following dependency to your pom:

````xml
<dependency>
    <groupId>de.heuboe.asfinag</groupId>
    <artifactId>vmis2-app-base</artifactId>
    <version>...</version>
</dependency>
````

## Features

### 1) CheckerHealthIndicator

An indicator used to contribute the health state of an application to its health endpoint. The health state is requested
from a configured akka actor.

#### Configure Spring

A CheckerHealthIndicator object can be configured in different ways. One way is to create the following bean in one
of your `@Configuration` annotated classes.

````java
import de.heuboe.asfinag.control.base.config.CheckerHealthIndicator;

@Bean
CheckerHealthIndicator checkerHealthIndicator() {
    // 42 is the timeout in ms of requesting the health state from the configured akka actor
    return new CheckerHealthIndicator(42);
}
````

Two other ways are either to add the `@Import(CheckerHealthIndicator.class)` annotation or the
`@ComponentScan(basePackages = {"de.heuboe.asfinag.control.base.config"})` annotation to one of your `@Configuration`
annotated classes. The main advantage of these variants is that you can directly configure the timeout via yaml or
properties.

Via yaml:

````yaml
de:
  heuboe:
    asfinag:
      control:
        base:
          health:
            # timeout in ms of requesting the health state from the configured akka actor (default: 5000)
            askStatusMsgTimeout: 42
````

Via properties:

````properties
# timeout in ms of requesting the health state from the configured akka actor (default: 5000)
de.heuboe.asfinag.control.base.health.askStatusMsgTimeout=42
````
