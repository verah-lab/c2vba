# VMIS2 Log4J2 Extension

This module contains classes that among other things allow using a customizable JSON-Layout in logs.

## Requirements

- Java 8 or later
- Log4J2 API + Core
- Jackson Databind

## Usage

Add the maven dependency to your project with the `runtime` scope:

````xml
<dependency>
    <groupId>de.heuboe.asfinag</groupId>
    <artifactId>vmis2-log4j2-extension</artifactId>
    <version>...</version>
    <scope>runtime</scope>
</dependency>
````

## JSON-Layout

### Usage

**Note:** The following configuration is not needed during development.

Setup your logging appenders configuration like this:

````xml
<File name="JsonFile" fileName="logs/app.log">
    <CustomJSONLayout />
</File>
````

or if you want to customize it, you can configure it like this:

````xml
<File name="JsonFile" fileName="logs/app.log">
    <CustomJSONLayout>
        <KeyValuePair key="applicationName" value="$${env:application.name:-N/A}" />
        <KeyValuePair key="applicationInstance" value="$${env:application.instance:-N/A}" />
        <KeyValuePair key="time" value="$${date:yyyy-MM-dd'T'HH:mm:ss.SSSZ}" />
        <KeyValuePair key="level" value="$${event:Level}" />
        <KeyValuePair key="thread" value="$${event:ThreadName}" />
        <KeyValuePair key="logger" value="$${event:Logger}" />
        <KeyValuePair key="source" value="$${event-plus:file}:$${event-plus:line}" />
        <KeyValuePair key="message" value="$${event:Message}" />
        <KeyValuePair key="call" value="$${event-plus:class}#$${event-plus:method}" />
        <KeyValuePair key="mdc" value="$${event-plus:mdc}" />
        <KeyValuePair key="exception" value="$${event-plus:exception}" />
        <KeyValuePair key="callstack" value="$${event-plus:stacktrace}" />
    </CustomJSONLayout>
</File>
````

If you don't specify any `KeyValuePair`s then it will use the default entries.

The value part of the keys can be used with any [lookups](https://logging.apache.org/log4j/2.0/manual/lookups.html) that
are supported by Log4J2.


### Configuration

The fields can be configured as follows:

- `KeyValuePair` (element)
  - `key` (attribute) string:  
    The key in the json output
  - `value` (attribute) string:  
    The value or template for the value that should be added to the json output. Supports lookups.  
    Currently all output are strings.

There also the following configuration options:

- `pretty` (attribute) boolean:  
  Whether to pretty print the JSON or not. Default `false`.
- `includeContextMap` (attribute) boolean:  
  Include the stringified context map in the json. Default `true`.
- `includeMarker` (attribute) boolean:
  Include the stringified marker in the json. Default `true`.

### Additional Lookups

This module adds the `event-plus` lookup which adds the following lookup keys:

- `time`
- `mdc`
- `exception`
- `stacktrace`
- `simpleClass`
- `class`
- `method`
- `file`
- `line`
