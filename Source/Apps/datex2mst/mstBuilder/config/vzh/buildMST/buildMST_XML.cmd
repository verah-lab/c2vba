set HBOUTIMMEDIATE=1

"C:\Program Files (x86)\Java\jre1.8.0_60\bin\java" ^
    -D__MST_PUB_BUILDER ^
    -Xmx512m ^
    -Xrs ^
    -Djava.library.path=d:\VZH\bin ^
    -Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB ^
    -Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton ^
    -DORBInitRef.NameService=corbaloc::he4-w7v:9923\NameService ^
    -classpath ^
    d:\VZH\jarsIntegrated\datex2MSTBuilder\*; ^
    de.heuboe.datex2.mst.builder.D2MSTPubBuilder ^
    -user=postgres ^
    -program=D2MSTPubBuilder ^
    -passwd=postgres ^
    -dbms=postgres ^
    -env=he4-w7v:5623:vzh ^
    -db=vzhdb06 ^
    -server=dbsrv9-2k12 ^
    -special=once ^
	-mstId=%1 ^
	-mstVersion=%2 ^
    -pubFilePath=d:\VZH\data\GEN_OUT\MST\%1 ^
    -pubErrFilePath=d:\VZH\data\GEN_OUT\MST\err ^
    -pubCountryCode=de ^
    -pubNatId="VZH" ^
    -pubLanguage=de ^
    -checkValidity ^
    -d2SchemaPath=d:\VZH\etc/d2Schema ^
    -d2SchemaFile=^
    -d2SchemaModelBaseVersion=^
    -d2SchemaNameSpace=^
    -d2SchemaCategory=^
    -useMVTemplateFactory ^
	-once ^
    +log=1A1  