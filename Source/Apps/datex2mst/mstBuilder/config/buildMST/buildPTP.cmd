set HBOUTIMMEDIATE=1
"C:\Program Files (x86)\Java\jre7\bin\java" -Xmx1024m -Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB -Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton -DORBInitRef.NameService=corbaloc::he1-w7v:9923/NameService -cp ../../jarsIntegrated/datex2PTPBuilder/*; de.heuboe.datex2.parking.table.pub.Main @ptp.arg -tableName=%1 -version=%2
pause