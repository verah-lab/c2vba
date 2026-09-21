# Changelog

## v2.0.0
- upgrade to java 17
- upgrade to SpringBoot 3
- update dependencies
  - de.heuboe.asfinag:vmis2-base-parent 2.13.1 -> 4.1.0
  - ${jprotoc.version} 4.4.0 -> 4.6.1
  - ${jmh.version} 1.33 -> 1.36

## v1.9.0

- Add support for kafka native deserializers
  - `ProtoDynamicMessageKafkaDeserializer`
  - `ProtoMessageKafkaDeserializer`
  - `ProtoPojoKafkaDeserializer`

## v1.8.0

- Update to [jprotoc v4.4.0](http://pdb.heuboe.hbintern/pdb/#version;id=37073) (was 4.3.1)
- Adds an optional header to kafka specifying the sending application
- Add support for header based deserialization
  - `HbProtoBufPojo` (Pojo base interface)
  - `Message` (Proto base interface)
  - `GeneratedMessageV3` (Proto base class)
  - `DynamicMessage` (Proto)
- Add support for lazy deserialization
  - `Lazy<SomeType>`

## v1.7.0

- Update to [vmis2-base-parent v2.13.1](http://pdb.heuboe.hbintern/pdb/#version;id=36704) (was 2.11.2)
- Update to [jprotoc v4.3.2](http://pdb.heuboe.hbintern/pdb/#version;id=34035) (was 4.0.4)
  - Dropped compatibility with interface projects that have been compiled with pre v4.1.0 versions
- Switch to new build pipeline

## v1.6.1

- Update to [vmis2-base-parent v2.11.2](http://pdb.heuboe.hbintern/pdb/#version;id=30998) (was 2.10.4)
- Update to [jprotoc v4.0.4](http://pdb.heuboe.hbintern/pdb/#version;id=30285) (was 4.0.3)
- Fix missing parameter in warning

## v1.6.0

- Update to [vmis2-base-parent v2.10.4](http://pdb.heuboe.hbintern/pdb/#version;id=30170) (was 2.10.3)
- Update to [jprotoc v4.0.3](http://pdb.heuboe.hbintern/pdb/#version;id=30217) (was 3.2.2)

## v1.5.2

- Update to [vmis2-base-parent v2.10.3](http://pdb.heuboe.hbintern/pdb/#version;id=29581) (was 2.8.4)
- Update to [jprotoc v3.2.2](http://pdb.heuboe.hbintern/pdb/#version;id=29425) (was 3.2.0)

## v1.5.1

- Allow registering custom `iidReader`s
- Improve error messages

## v1.5.0

- Update to [vmis2-base-parent v2.8.4](http://pdb.heuboe.hbintern/pdb/#version;id=29274) (was 2.4.0)
- Update to [jprotoc v3.2.0](http://pdb.heuboe.hbintern/pdb/#version;id=29275) (was 2.4.0)
- Automatically send `X-IID` header (if possible)

## v1.4.0

**Changes**

- Update to [vmis2-base-parent v2.4.0](http://pdb.heuboe.hbintern/pdb/#version;id=28330) (was 2.3.6)
- Update to [jprotoc v2.4.0](http://pdb.heuboe.hbintern/pdb/#version;id=28335) (was 2.2.3)
- Throw exception with a helpful error message in case of configuration errors (if possible)

**Migration**

- Use jprotoc 2.3.0 or later

## v1.3.0

**Changes**

- Update to [vmis2-base-parent v2.3.6](http://pdb.heuboe.hbintern/pdb/#version;id=27876) (was 2.3.5)
- Update to [jprotoc v2.2.3](http://pdb.heuboe.hbintern/pdb/#version;id=27881) (was 2.2.0)
- Add exception handling when proto parsing fails (compare message types)
- Use headers in the deserializer

**Migration**

- Use jprotoc 2.2.3 or later

## v1.2.0

- Update to vmis2-base-parent 2.3.5 (was 2.3.2)
- Update to jprotoc 2.2.0 (was 2.0.5)
- Move most of the meta logic to jprotoc classes
- Fix `IllegalStateException: Recursive update` in `ProtoPojoKafkaMessageConverter#initialRecordHeaders(...)`
- Add spring auto configuration

## v1.1.0

- Update to vmis2-base-parent 2.3.2 (was 2.1.0)
- Update to jprotoc 2.0.5 (was 1.1.1)
- Add type headers to kafka messages

## v1.0.3

- Fix javadoc warnings
- Fix sonar warnings
- Update to vmis2-base-parent 2.1.0 (was 2.0.2)

## v1.0.2

- Fix concurrency issue

## v1.0.1

- Check java 11 compatibility
- Update jprotoc tools

## v1.0.0

Initial release
