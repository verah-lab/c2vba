# tls-legacy-data-converter

The data converter is necessary in the use case of NI34. There are several legacy devices that only support TLS 1993 
FG 4 standard. To support these legacy devices a conversion of FG 4 control data is necessary.

The following schematic shows how the conversion process handles the legacy data:  

![Type48-55-Handling.png](contrib%2FType48-55-Handling.png)


## Backlog
It is necessary to extend the mapping in send direction to the corresponding Job topics
- Stellzustand48Job -> StellzustandJob
- Stellzustand55Job -> StellzustandJob
- Grundeinstellung32Job -> GrundeinstellungJob
- Grundeinstellung33Job -> GrundeinstellungJob