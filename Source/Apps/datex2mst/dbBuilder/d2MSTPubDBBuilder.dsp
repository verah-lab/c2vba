# Microsoft Developer Studio Project File - Name="d2MSTPubDBBuilder" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 6.00
# ** DO NOT EDIT **

# TARGTYPE "Win32 (x86) Console Application" 0x0103

CFG=d2MSTPubDBBuilder - Win32 Release
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "d2MSTPubDBBuilder.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "d2MSTPubDBBuilder.mak" CFG="d2MSTPubDBBuilder - Win32 Release"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "d2MSTPubDBBuilder - Win32 Release" (based on "Win32 (x86) Console Application")
!MESSAGE "d2MSTPubDBBuilder - Win32 Debug" (based on "Win32 (x86) Console Application")
!MESSAGE 

# Begin Project
# PROP AllowPerConfigDependencies 0
# PROP Scc_ProjName "d2MSTPubDBBuilder"
# PROP Scc_LocalPath "."
CPP=cl.exe
RSC=rc.exe

!IF  "$(CFG)" == "d2MSTPubDBBuilder - Win32 Release"

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
# ADD LINK32 /nologo /subsystem:console /debug /machine:I386 /nodefaultlib:"LIBC.lib" /out:"Y:\linkVobW\hessen\bin\d2MSTPubDBBuilder.exe" /pdbtype:sept /libpath:"../../../lib" /libpath:"..\..\..\..\support\lib\nt386\vc60"

!ELSEIF  "$(CFG)" == "d2MSTPubDBBuilder - Win32 Debug"

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
# ADD LINK32 loc_man_client_d.lib /nologo /subsystem:console /incremental:no /debug /machine:I386 /nodefaultlib:"loc_man_d.lib" /out:"..\..\..\bin\d2MSTPubDBBuilder.exe" /pdbtype:sept /libpath:"../../../lib" /libpath:"..\..\..\..\support\lib\nt386\vc60"

!ENDIF 

# Begin Target

# Name "d2MSTPubDBBuilder - Win32 Release"
# Name "d2MSTPubDBBuilder - Win32 Debug"
# Begin Group "Source Files"

# PROP Default_Filter "cpp;c;cxx;rc;def;r;odl;idl;hpj;bat"
# Begin Source File

SOURCE=.\d2MSTDbBuilderIfaceImpl.cpp
# End Source File
# Begin Source File

SOURCE=.\d2MSTPubDBBuilder.cpp
# End Source File
# Begin Source File

SOURCE=.\ddpmap.cpp
# End Source File
# Begin Source File

SOURCE=.\ddpmapDDP.cpp
# End Source File
# Begin Source File

SOURCE=.\handler.cpp
# End Source File
# Begin Source File

SOURCE=.\item.cpp
# End Source File
# Begin Source File

SOURCE=.\loc_match.cpp
# End Source File
# Begin Source File

SOURCE=.\psError.cpp
# End Source File
# Begin Source File

SOURCE=.\psString.cpp
# End Source File
# Begin Source File

SOURCE=.\psTimeMan.cpp
# End Source File
# Begin Source File

SOURCE=.\res.rc
# End Source File
# Begin Source File

SOURCE=.\state.cpp
# End Source File
# Begin Source File

SOURCE=.\Value.cpp
# End Source File
# Begin Source File

SOURCE=.\ValueBase.cpp
# End Source File
# Begin Source File

SOURCE=.\VBase.cpp
# End Source File
# End Group
# Begin Group "Header Files"

# PROP Default_Filter "h;hpp;hxx;hm;inl"
# Begin Source File

SOURCE=.\d2Conf.h
# End Source File
# Begin Source File

SOURCE=.\d2MSTDbBuilderIfaceImpl.h
# End Source File
# Begin Source File

SOURCE=.\ddpmap.h
# End Source File
# Begin Source File

SOURCE=.\ddpmapDDP.h
# End Source File
# Begin Source File

SOURCE=.\handler.h
# End Source File
# Begin Source File

SOURCE=.\handler_l.h
# End Source File
# Begin Source File

SOURCE=.\item.h
# End Source File
# Begin Source File

SOURCE=.\loc_match.h
# End Source File
# Begin Source File

SOURCE=.\psError.h
# End Source File
# Begin Source File

SOURCE=.\psIterator.h
# End Source File
# Begin Source File

SOURCE=.\psString.h
# End Source File
# Begin Source File

SOURCE=.\psTimeMan.h
# End Source File
# Begin Source File

SOURCE=.\resource.h
# End Source File
# Begin Source File

SOURCE=.\state.h
# End Source File
# Begin Source File

SOURCE=.\tr_err.h
# End Source File
# Begin Source File

SOURCE=.\trMisc.h
# End Source File
# Begin Source File

SOURCE=.\Value.h
# End Source File
# Begin Source File

SOURCE=.\ValueBase.h
# End Source File
# Begin Source File

SOURCE=.\VBase.h
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

SOURCE=.\config.txt
# End Source File
# Begin Source File

SOURCE=.\configXML.txt
# End Source File
# Begin Source File

SOURCE=.\configXML.xml
# End Source File
# Begin Source File

SOURCE=.\d2MSTPubDBBuilder.arg
# End Source File
# Begin Source File

SOURCE=..\supplyDDP\d2MSTSupply.ddl

!IF  "$(CFG)" == "d2MSTPubDBBuilder - Win32 Release"

!ELSEIF  "$(CFG)" == "d2MSTPubDBBuilder - Win32 Debug"

# Begin Custom Build
InputPath=..\supplyDDP\d2MSTSupply.ddl
InputName=d2MSTSupply

"gd_ddp_$(InputName).h" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
	ddl -header -clearcase -addIncl=..\..\..\ddl -dic=..\..\..\..\support\ddl-temp\dic $(InputPath)

# End Custom Build

!ENDIF 

# End Source File
# Begin Source File

SOURCE=.\def_lane.xml
# End Source File
# Begin Source File

SOURCE=.\def_tvupdsgldata.xml
# End Source File
# Begin Source File

SOURCE=.\join_mpt.txt
# End Source File
# Begin Source File

SOURCE=.\makefile
# End Source File
# Begin Source File

SOURCE=.\MSTDef.xsd
# End Source File
# Begin Source File

SOURCE=.\ToDo.txt
# End Source File
# End Target
# End Project
