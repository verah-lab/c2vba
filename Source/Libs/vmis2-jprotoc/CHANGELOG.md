# Changelog

## v4.6.2

**Changed**

* `de.heuboe.asfinag:vmis2-base-parent .............. 2.24.0 -> 2.25.0`

**Fixed**

* [issue #18](https://gitlab.heuboe.hbintern/VMIS2/base/vmis2-jprotoc/-/issues/18) - Support UNRECOGNIZED enum value in pojos

## v4.6.1

* Support `proto.transfer.registry.initialization.disabled` system property to disable BaseTransfer-Class caching in the `ProtoTransferRegistry` class  
  
  _Background information:_  
  This disables the scan of the classpath for ProtoTransferCatalogs and the caching of all BaseTransfer instances in the ProtoTransferRegistry class which saves a lot of memory, especially if you have many Proto classes in the classpath.
  However, it makes the use of the ProtoTransferRegistry class obsolete, since it no longer caches any information.  
  If you need a transfer class to a specific proto, you have to find an alternative way in that case.
  The `ProtoPojoUtils` class, which is usually used when such operations are needed, has various fallback mechanisms for this and can still be used - even if the ProtoTransferRegistry class has not been initialized with the BaseTransfer classes.

  As a rule of thumb, if you have only a few proto classes in the classpath, or are likely to use most proto classes in the classpath, then stick with the default.

  But if you have many proto classes in the classpath and use only a few of them, then disable the ProtoTransferRegistry initialization and observe whether this significantly changes the memory consumption of your application.
  In this case it might be useful to disable the initialization of the ProtoTransferRegistry.

## v4.6.0

- resolve [issue #15](https://gitlab.heuboe.hbintern/VMIS2/base/vmis2-jprotoc/-/issues/15) Support null values for scalar message fields in POJOs All Args Constructor.

## v4.5.0

- Update to [vmis2-base-parent v2.14.0](http://pdb.heuboe.hbintern/pdb/#version;id=37611) (was 2.13.1)

## v4.4.0

- Update to [vmis2-base-parent v2.13.1](http://pdb.heuboe.hbintern/pdb/#version;id=36704) (was 2.11.7)
- Switch to HB build pipeline
- Add constant for `X-Origin` header

## v4.3.2

- Add `X-REF-IID` Kafka header constant

## v4.3.1

- Add support for configuring `--ignoreAbsentStereotype` with an optional value (`=true` or `=false`)
  - This allows it to be configured via pom property.

## v4.3.0

- Update to [vmis2-base-parent v2.11.7](http://pdb.heuboe.hbintern/pdb/#version;id=33089) (was 2.11.6)
- Add ProtoStereotypeAsserter (turned on by default) (#14)

## v4.2.2

- Fix handling of boolean fields starting with "is" (#12)

## v4.2.1

- Update to [vmis2-base-parent v2.11.6](http://pdb.heuboe.hbintern/pdb/#version;id=32685) (was 2.11.3)
- Only validate files contained in the current project to avoid issues due to #13

## v4.2.0

- Update to [vmis2-base-parent v2.11.3](http://pdb.heuboe.hbintern/pdb/#version;id=31193) (was 2.10.4)
- Add `SERVICE_MESSAGE` to `Stereotype`

## v4.1.0

**Changes**

- Removed backwards compatibility code
- Narrow types in transfer sub-interfaces

**Note**

Libraries that work with the `Transfer` instances are not compatible with interfaces
that have been compiled with an earlier version of this library.  
But the other way round isn't a problem.

## v4.0.4

- Update to [vmis2-base-parent v2.10.4](http://pdb.heuboe.hbintern/pdb/#version;id=30217) (was 2.10.3)
- Fixed enum conversion behavior for `UNRECOGNIZED`

## v4.0.3

- Change `required` default value from `false` to `true`
- Rename `getMessageType` to `getStereotype`

## v4.0.2

- Rename `MessageType` to `Stereotype`

## v4.0.1

- Update to [vmis2-base-parent v2.10.3](http://pdb.heuboe.hbintern/pdb/#version;id=30027) (was 2.10.2)
- Fake `ExternalReferenceProto` for backwards compatibility for previously generated protos

## v4.0.0

- Update to [vmis2-base-parent v2.10.2](http://pdb.heuboe.hbintern/pdb/#version;id=29960) (was 2.10.0)
- Rename `ExternalReferenceProto` to `DocumentationProto`
- Added `messageType` message option
- Added `required` field option
- Added utility classes for the contained proto features

## v3.3.0

- Update to [vmis2-base-parent v2.10.0](http://pdb.heuboe.hbintern/pdb/#version;id=29946) (was 2.9.1)
- Add support for references to `enum` types.

## v3.2.2

- Update to [vmis2-base-parent v2.9.1](http://pdb.heuboe.hbintern/pdb/#version;id=29423) (was 2.8.4)
- Improve error message/handling when ProtoTransferRegistry initialization fails.

## v3.2.1

Broken Release

## v3.2.0

- Update to [vmis2-base-parent v2.8.4](http://pdb.heuboe.hbintern/pdb/#version;id=29274) (was 2.7.0)
- Add the ability to use custom `ClassLoader`s
- Add `IIDContainer` interface

## v3.1.0

- Update to [vmis2-base-parent v2.7.0](http://pdb.heuboe.hbintern/pdb/#version;id=28798) (was 2.6.1)

## v3.0.1

- Fix broken `ProtoExternalReferenceAsserter` when loading proto descriptors from input/files

## v3.0.0

**Changes**

- [BREAKING] Add `@Document` annotation to pojos (removes `P` prefix (noise) from collection names)

**Migration**

- Adjust collection names in the mongodb (Remove `P` prefix)

## v2.5.0

- Add `@Id` annotation to properties named `iid`
- Update to [vmis2-base-parent v2.6.1](http://pdb.heuboe.hbintern/pdb/#version;id=28570) (was 2.5.0)

## v2.4.2

- Fixed more cases for the OuterClassname generation
- Fixed a bug with enums with field numbers >= 1000

## v2.4.1

- Update to [vmis2-base-parent v2.5.0](http://pdb.heuboe.hbintern/pdb/#version;id=28443) (was 2.4.0)
- Fixed OuterClassname generation if the name contains numbers ([#7](https://gitlab.heuboe.hbintern/VMIS2/base/vmis2-jprotoc/issues/7))
- Updated dependencies

## v2.4.0

- Add support for `extRef` hints and their validation
- Update to [vmis2-base-parent v2.4.0](http://pdb.heuboe.hbintern/pdb/#version;id=28330) (was 2.3.6) 

## v2.3.1

- Add `--ignoreAbsentVersion` flag to ignore absent version specifications in proto files.

## v2.3.0

**Changes**

- Add `interfaceVersion` to the transferinterface
- Add the ability to assert the `interfaceVersion` in the protos.
- Remove some deprecated code

**Usage**

````proto
import "heuboe/protobuf/InterfaceVersion.proto";

option (.heuboe.protobuf.interface_version) = "<YourVersion>";
````
