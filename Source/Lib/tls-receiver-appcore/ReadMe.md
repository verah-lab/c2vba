# VMIS2 TLS receiver

**Table Of Contents**

- [VMIS2 TLS receiver](#vmis2-tls-receiver)
  - [About](#about)
  - [Features](#features)
    - [Sub feature](#sub-feature)

## About

This project is the main part of the tls receiver.
It contains the main function, partial configuration and more

The following components are included:
* address converter (by use of config service lib) // rcvadrcvt
* receive data converter interface // cvtinterface
* receive data converter // rcv.data.converter ...
* data writer // datawriter 
* telegram input // teleinin // tele.in

## Properties

### de.heuboe.asfinag.tls.receiver.timeoffset
This property specifies the time offset in seconds for all time items in DE-Blocks if not detailed as below.
It will result in a timeshift of incoming data.<br>
Value is an integer specifying the time offset in seconds.<br>
Example: de.heuboe.asfinag.tls.receiver.timeoffset=-3600

### de.heuboe.asfinag.tls.receiver.timeoffset-map
Each entry in the string must have the format: fg/id/typ!offset<br>
Whitespace is ignored anywhere in the string.<br>
Those offsets are applied to all time items in DE-Blocks of the given TlsTypeId (fg/id/typ).<br>
Example: de.heuboe.asfinag.tls.receiver.timeoffset-map=4/131/120!90, 4/133/120!-30 , 1/132 / 207 !120<br>


