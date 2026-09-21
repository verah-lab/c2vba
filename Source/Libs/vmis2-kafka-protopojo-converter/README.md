# Kafka-ProtoPojo-Converter

- [Kafka-ProtoPojo-Converter](#kafka-protopojo-converter)
  - [About](#about)
  - [Development-Requirements](#development-requirements)
  - [Runtime-Requirements](#runtime-requirements)
    - [Optional](#optional)
  - [Usage](#usage)
    - [Usage in Spring](#usage-in-spring)
      - [1) Configure Spring](#1-configure-spring)
      - [2) Configure Kafka](#2-configure-kafka)
- [Optionally, if and only if you wish to distinguish between different producers (`X-Origin` header)](#optionally-if-and-only-if-you-wish-to-distinguish-between-different-producers-x-origin-header)
- [Optionally, if and only if you wish to distinguish between different producers (`X-Origin` header)](#optionally-if-and-only-if-you-wish-to-distinguish-between-different-producers-x-origin-header-1)
      - [3) Receiving messages](#3-receiving-messages)
      - [4) Sending messages](#4-sending-messages)
      - [5) Receiving and responding messages in one](#5-receiving-and-responding-messages-in-one)
    - [Usage in plain Kafka](#usage-in-plain-kafka)

## About

The Kafka-ProtoPojo-Converter is a library for Spring-Kafka that provides a MessageConverter that can convert
Kafka-Messages to and from the expected type alone.

## Development-Requirements

- Java 17 or later
- Maven 3.6 or later
- IDE with Annotation-Processing enabled
  - For eclipse use and enable `m2e-apt`

## Runtime-Requirements

- Java 17

### Optional

- Jackson-Databind for JSON-Support (or XML...)

## Usage

Add the following dependency to your pom:

````xml
<dependency>
    <groupId>de.heuboe.asfinag</groupId>
    <artifactId>vmis2-kafka-protopojo-converter</artifactId>
    <version>...</version>
</dependency>
````

### Usage in Spring

#### 1) Configure Spring

You can skip this step if you use spring's auto-configuration.

Create the following bean in one of your `@Configuration` annotated classes  (or add that class's package to the classpath scanning).

````java
import de.heuboe.vmis2.kafka.converter.ProtoPojoKafkaMessageConverter;

@Bean
ProtoPojoKafkaMessageConverter messageConverter() {
    return new ProtoPojoKafkaMessageConverter();
}
````

In case you want to add a default `origin Header` to your published Kafka record (to document which application wrote the record) use:

```java
@Bean
ProtoPojoKafkaMessageConverter messageConverter() {
    return new ProtoPojoKafkaMessageConverter("<myApplication>");
}
```


#### 2) Configure Kafka

Make sure that you have configured your Apache Kafka (de-)serializers to passthrough.

Either via yaml:

````yaml
spring:
  kafka:
    consumer:
      properties:
        value-deserializer: org.apache.kafka.common.serialization.ByteArrayDeserializer
    producer:
      properties:
        value-serializer: org.apache.kafka.common.serialization.ByteArraySerializer

# Optionally, if and only if you wish to distinguish between different producers (`X-Origin` header)
de:
  heuboe:
    kafka:
      producer:
        name: my-application
````

or via properties:

````properties
spring.kafka.consumer.properties.value.deserializer=org.apache.kafka.common.serialization.ByteArrayDeserializer
spring.kafka.producer.properties.value.serializer=org.apache.kafka.common.serialization.ByteArraySerializer

# Optionally, if and only if you wish to distinguish between different producers (`X-Origin` header)
de.heuboe.kafka.producer.name=my-application
````

#### 3) Receiving messages

Add `@EnableKafka` to one of your `@Configuration` classes.

````java
@KafkaListener(topics = "<NameOfYourInputTopic>")
public void receive(final MyExpectedMessage payload) {
    LOGGER.debug("Received payload='{}'", payload);
}
````

If you want to access the header from within your listener you can do it like this:

````java
@KafkaListener(topics = PROTO_TOPIC)
public void listener(Proto proto, @Header(name = HEADER_PROTOBUF_TYPE, required = false) byte[] type) {
````

Supported types:

- protobuf classes
- pojo classes
- `HbProtoBufPojo` (Pojo base interface)
- `Message` (Proto base interface)
- `GeneratedMessageV3` (Proto base class)
- `DynamicMessage` (Proto)
- `Lazy<SomeType>` (lazyily deserialized)

#### 4) Sending messages

Important: You have to send it using a ``Message`` otherwise it will not pass through Spring's
``MessageConverter`` but will be instead piped directly through Kafka (which causes a `ClassCastException`).

````java
public ListenableFuture<SendResult<String, Object>> send(final MyResponseMessage response) {
    LOGGER.debug("Sending response: " + response);
    return this.kafkaTemplate.send(MessageBuilder.withPayload(response)
            .setHeader(KafkaHeaders.TOPIC, "<NameOfYourOutputTopic>")
            .build());
}
````

**Note:** The messages will be send asynchronously. If you want to wait for them to be send use `get()` on the
`ListenableFuture`.

**Note:** If you send your protobuf message directly as bytes,
then you need to add the `X-Protobuf-Type` to the message.

````java
messageBuilder.setHeader(ProtoPojoKafkaMessageConverter.HEADER_PROTOBUF_TYPE,
        ProtoClass.getDescriptor().getFullName().getBytes(StandardCharsets.UTF_8));
````

It is important that these headers are sent as bytes,
as strings and other types are Json encoded and therefore start and end with quotes (`"`), causing parsing problems.

#### 5) Receiving and responding messages in one

Add `@EnableKafka` to one of your `@Configuration` classes.

````java
@KafkaListener(topics = "<NameOfYourInputTopic>")
@SendTo("<NameOfYourOutputTopic>")
public MyResponseMessage receive(final MyExpectedMessage payload) {
    LOGGER.debug("Received payload='{}'", payload);
    MyResponseMessage response = magic(payload);
    LOGGER.debug("Sending response: " + response);
    return response;
}
````

### Usage in plain Kafka

Use any of the following classes as needed:

- `de.heuboe.vmis2.kafka.converter.ProtoPojoKafkaDeserializer` (-> `HbProtoBufPojo` (Pojo))
- `de.heuboe.vmis2.kafka.converter.ProtoMessageKafkaDeserializer` (-> `GeneratedMessageV3` (Proto))
- `de.heuboe.vmis2.kafka.converter.ProtoDynamicMessageKafkaDeserializer` (-> `DynamicMessage`)
