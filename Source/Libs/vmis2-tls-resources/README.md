# vmis2-tls-resourecs

## Description

This project generates basic resources for the project VMIS2. It provides generators to convert TLS scripts into 
different kinds of files.

## Generator types

### Java Google Proto Buffer generator

The proto generator will be used to create Java Google Proto Buffer files and provide usable pojo java classes. Its main
class for execution can be found in `Vmis2ProtoGenerator.java` in package `vmis2-tls-generator-proto`. The generated
proto files will be placed in the package `vmis2-tls-proto-interface` in the folder `proto`. While building the project 
the proto files will be compiled to java classes.


### Junit receiver and sender test generator

The Junit receiver and sender test generator will be used to create Junit test files for all TLS scripts. The tests
are not directly usable. Parts of the tests must be manually updated.


## TLS scripts

The module `vmis2-tls-scripts` contains the TLS scripts that are used for the described generators above and are not 
part of the core scripts in `tls-scripts`.


##Generated Nuget:
 * VMIS2.TLS.Resources.Proto (dependent of TLS.Resources.Proto) 
