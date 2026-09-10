set HBOUTIMMEDIATE=1
"C:\Program Files (x86)\Java\jre1.8.0_66\bin\java" -Xmx1024m  -cp ../../../jarsIntegrated/vmsTablePublication/*; de.heuboe.datex2.vms.table.pub.Main @sdbby_all.arg -tableId=%1 -tableName=%1 -tableVersion=%2
pause
