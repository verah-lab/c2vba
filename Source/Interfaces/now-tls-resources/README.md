# now-tls-resources

## Description

This project generates basic resources for the project NOW. It provides generators to convert TLS scripts into
different kinds of files.

## Generator types

### Java Google Proto Buffer generator

The proto generator will be used to create Java Google Proto Buffer files and provide usable pojo java classes. Its main
class for execution can be found in `NowProtoGenerator.java` in package `now-tls-generator-proto`. The generated
proto files will be placed in the package `now-tls-proto-interface` in the folder `proto`. While building the project
the proto files will be compiled to java classes.


### Junit receiver and sender test generator

The Junit receiver and sender test generator will be used to create Junit test files for all TLS scripts. The tests
are not directly usable. Parts of the tests must be manually updated.


## TLS scripts

The module `now-tls-scripts` contains the TLS scripts that are used for the described generators above. The containing
`send-fg-all.txt` script will override the same file from `tls-scripts`. The difference between the basic send script is
the 
