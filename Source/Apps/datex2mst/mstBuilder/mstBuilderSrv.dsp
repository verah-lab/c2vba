# Microsoft Developer Studio Project File - Name="mstBuilderSrv" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 6.00
# ** DO NOT EDIT **

# TARGTYPE "Win32 (x86) Console Application" 0x0103

CFG=mstBuilderSrv - Win32 Release
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "mstBuilderSrv.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "mstBuilderSrv.mak" CFG="mstBuilderSrv - Win32 Release"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "mstBuilderSrv - Win32 Release" (based on "Win32 (x86) Console Application")
!MESSAGE "mstBuilderSrv - Win32 Debug" (based on "Win32 (x86) Console Application")
!MESSAGE 

# Begin Project
# PROP AllowPerConfigDependencies 0
# PROP Scc_ProjName "mstBuilderSrv"
# PROP Scc_LocalPath "."
CPP=cl.exe
RSC=rc.exe

!IF  "$(CFG)" == "mstBuilderSrv - Win32 Release"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir "Release"
# PROP BASE Intermediate_Dir "Release"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 0
# PROP Output_Dir "Release"
# PROP Intermediate_Dir "Release"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
# ADD BASE CPP /nologo /W3 /GX /O2 /D "WIN32" /D "NDEBUG" /D "_CONSOLE" /D "_MBCS" /YX /FD /c
# ADD CPP /nologo /MD /W3 /GR /GX /Zi /I "..\..\..\..\..\support\incl\nt\xercesc250" /I "../../../incl" /I "../../../corba" /I "..\..\..\..\..\support\incl\nt" /D "WIN32" /D "NDEBUG" /D "DEBUG" /D "_CONSOLE" /D "_MBCS" /D _WIN32_WINNT=0x0400 /YX /FD /c
# ADD BASE RSC /l 0x407 /d "NDEBUG"
# ADD RSC /l 0x407 /d "NDEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /subsystem:console /machine:I386
# ADD LINK32 /nologo /subsystem:console /debug /machine:I386 /nodefaultlib:"LIBC.lib" /out:"Y:\linkVobW\hessen\bin\mstBuilderSrv.exe" /pdbtype:sept /libpath:"../../../lib" /libpath:"..\..\..\..\support\lib\nt386\vc60"

!ELSEIF  "$(CFG)" == "mstBuilderSrv - Win32 Debug"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir "Debug"
# PROP BASE Intermediate_Dir "Debug"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 1
# PROP Output_Dir "Debug"
# PROP Intermediate_Dir "Debug"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
# ADD BASE CPP /nologo /W3 /Gm /GX /ZI /Od /D "WIN32" /D "_DEBUG" /D "_CONSOLE" /D "_MBCS" /YX /FD /GZ /c
# ADD CPP /nologo /MDd /W3 /GR /GX /Zi /Od /I "..\..\..\..\..\support\incl\nt\xercesc250" /I "../../../incl" /I "../../../corba" /I "..\..\..\..\..\support\incl\nt" /D "WIN32" /D "_DEBUG" /D "_CONSOLE" /D "_MBCS" /D _WIN32_WINNT=0x0400 /Fr /YX /FD /GZ /c
# ADD BASE RSC /l 0x407 /d "_DEBUG"
# ADD RSC /l 0x407 /d "_DEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /subsystem:console /debug /machine:I386 /pdbtype:sept
# ADD LINK32 loc_man_client_d.lib /nologo /subsystem:console /incremental:no /debug /machine:I386 /nodefaultlib:"loc_man_d.lib" /out:"..\..\..\bin\mstBuilderSrv.exe" /pdbtype:sept /libpath:"../../../lib" /libpath:"..\..\..\..\support\lib\nt386\vc60"

!ENDIF 

# Begin Target

# Name "mstBuilderSrv - Win32 Release"
# Name "mstBuilderSrv - Win32 Debug"
# Begin Group "Source Files"

# PROP Default_Filter "cpp;c;cxx;rc;def;r;odl;idl;hpj;bat"
# Begin Source File

SOURCE=.\loc_match.cpp
# End Source File
# Begin Source File

SOURCE=.\loc_match.h
# End Source File
# Begin Source File

SOURCE=.\mbMapping.cpp
# End Source File
# Begin Source File

SOURCE=.\mbMapping.h
# End Source File
# Begin Source File

SOURCE=.\mbMst.h
# End Source File
# Begin Source File

SOURCE=.\mstBuilderImpl.cpp
# End Source File
# Begin Source File

SOURCE=.\mstBuilderImpl.h
# End Source File
# Begin Source File

SOURCE=.\mstBuilderSrv.cpp
# End Source File
# Begin Source File

SOURCE=.\res.rc
# End Source File
# Begin Source File

SOURCE=.\resource.h
# End Source File
# End Group
# Begin Group "defParser"

# PROP Default_Filter ""
# Begin Source File

SOURCE=.\baseXMLBase.cpp
# End Source File
# Begin Source File

SOURCE=.\baseXMLBase.h
# End Source File
# Begin Source File

SOURCE=.\baseXMLConfig.h
# End Source File
# Begin Source File

SOURCE=.\baseXMLUtil.h
# End Source File
# Begin Source File

SOURCE=.\xmlDefReader.cpp
# End Source File
# Begin Source File

SOURCE=.\xmlDefReader.h
# End Source File
# End Group
# Begin Source File

SOURCE=.\arg.txt
# End Source File
# Begin Source File

SOURCE=.\argDescr.txt
# End Source File
# Begin Source File

SOURCE=.\makefile
# End Source File
# Begin Source File

SOURCE=.\mstBuilder.ddl

!IF  "$(CFG)" == "mstBuilderSrv - Win32 Release"

!ELSEIF  "$(CFG)" == "mstBuilderSrv - Win32 Debug"

# Begin Custom Build
InputPath=.\mstBuilder.ddl
InputName=mstBuilder

"gd_ddp_$(InputName).h" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
	ddl -header -clearcase -addIncl=..\..\..\ddl -dic=..\..\..\..\support\ddl-temp\dic $(InputPath)

# End Custom Build

!ENDIF 

# End Source File
# Begin Source File

SOURCE=.\mstBuilderSrv.arg
# End Source File
# Begin Source File

SOURCE=.\mstConfig.xsd
# End Source File
# Begin Source File

SOURCE=.\mstConfigBsp.xml
# End Source File
# Begin Source File

SOURCE=.\ToDo.txt
# End Source File
# End Target
# End Project
