# Microsoft Developer Studio Project File - Name="mstBuilderLib" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 6.00
# ** DO NOT EDIT **

# TARGTYPE "Win32 (x86) Static Library" 0x0104

CFG=mstBuilderLib - Win32 Debug STLport
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "mstBuilderLib.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "mstBuilderLib.mak" CFG="mstBuilderLib - Win32 Debug STLport"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "mstBuilderLib - Win32 Release" (based on "Win32 (x86) Static Library")
!MESSAGE "mstBuilderLib - Win32 Debug" (based on "Win32 (x86) Static Library")
!MESSAGE "mstBuilderLib - Win32 Debug STLport" (based on "Win32 (x86) Static Library")
!MESSAGE "mstBuilderLib - Win32 Release STLport" (based on "Win32 (x86) Static Library")
!MESSAGE 

# Begin Project
# PROP AllowPerConfigDependencies 0
# PROP Scc_ProjName "mstBuilderLib"
# PROP Scc_LocalPath "."
CPP=cl.exe
RSC=rc.exe

!IF  "$(CFG)" == "mstBuilderLib - Win32 Release"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir "Release"
# PROP BASE Intermediate_Dir "Release"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 0
# PROP Output_Dir "Release"
# PROP Intermediate_Dir "Release"
# PROP Target_Dir ""
MTL=midl.exe
LINK32=link.exe
# ADD BASE CPP /nologo /W3 /GX /O2 /D "WIN32" /D "NDEBUG" /D "_MBCS" /D "_LIB" /YX /FD /c
# ADD CPP /nologo /MD /W3 /GR /GX /Zi /Od /I "..\..\..\incl" /I "..\..\..\corba" /I "..\..\..\..\..\support\incl\nt" /D "WIN32" /D "NDEBUG" /D "_MBCS" /D "_LIB" /D "DEBUG" /D _WIN32_WINNT=0x0400 /YX /FD /c
# ADD BASE RSC /l 0x407 /d "NDEBUG"
# ADD RSC /l 0x407 /d "NDEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LIB32=link.exe -lib
# ADD BASE LIB32 /nologo
# ADD LIB32 /nologo /out:"..\..\..\..\lib\mstBuilderLib.lib"

!ELSEIF  "$(CFG)" == "mstBuilderLib - Win32 Debug"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir "Debug"
# PROP BASE Intermediate_Dir "Debug"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 1
# PROP Output_Dir "Debug"
# PROP Intermediate_Dir "Debug"
# PROP Target_Dir ""
MTL=midl.exe
LINK32=link.exe
# ADD BASE CPP /nologo /W3 /Gm /GX /ZI /Od /D "WIN32" /D "_DEBUG" /D "_MBCS" /D "_LIB" /YX /FD /GZ /c
# ADD CPP /nologo /MDd /W3 /Gm /GR /GX /Zi /Od /I "..\..\..\incl" /I "..\..\..\corba" /I "..\..\..\..\..\support\incl\nt" /D "WIN32" /D "_DEBUG" /D "_MBCS" /D "_LIB" /D "DEBUG" /D _WIN32_WINNT=0x0400 /YX /FD /GZ /c
# ADD BASE RSC /l 0x407 /d "_DEBUG"
# ADD RSC /l 0x407 /d "_DEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LIB32=link.exe -lib
# ADD BASE LIB32 /nologo
# ADD LIB32 /nologo /out:"..\..\..\..\lib\mstBuilderLib_d.lib"

!ELSEIF  "$(CFG)" == "mstBuilderLib - Win32 Debug STLport"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir "mstBuilderLib___Win32_Debug_STLport"
# PROP BASE Intermediate_Dir "mstBuilderLib___Win32_Debug_STLport"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 1
# PROP Output_Dir "Debug_STLport"
# PROP Intermediate_Dir "Debug_STLport"
# PROP Target_Dir ""
MTL=midl.exe
LINK32=link.exe
# ADD BASE CPP /nologo /MDd /W3 /Gm /GR /GX /Zi /Od /I "..\..\..\..\incl" /I "..\..\..\..\corba" /I "..\..\..\..\..\support\incl\nt" /D "WIN32" /D "_DEBUG" /D "_MBCS" /D "_LIB" /D "DEBUG" /YX /FD /GZ /c
# ADD CPP /nologo /MDd /W3 /Gm /GR /GX /Zi /Od /I "..\..\..\incl" /I "..\..\..\corba" /I "..\..\..\..\support\incl\nt" /D "WIN32" /D "_DEBUG" /D "_MBCS" /D "_LIB" /D "DEBUG" /D "USE_STLPORT" /YX /FD /GZ /c
# ADD BASE RSC /l 0x407 /d "_DEBUG"
# ADD RSC /l 0x407 /d "_DEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LIB32=link.exe -lib
# ADD BASE LIB32 /nologo /out:"..\..\..\..\lib\mstBuilderLib_d.lib"
# ADD LIB32 /nologo /out:"..\..\..\lib\mstBuilderLibs_d.lib"

!ELSEIF  "$(CFG)" == "mstBuilderLib - Win32 Release STLport"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir "mstBuilderLib___Win32_Release_STLport"
# PROP BASE Intermediate_Dir "mstBuilderLib___Win32_Release_STLport"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 0
# PROP Output_Dir "Release_STLport"
# PROP Intermediate_Dir "Release_STLport"
# PROP Target_Dir ""
MTL=midl.exe
LINK32=link.exe
# ADD BASE CPP /nologo /MD /W3 /GR /GX /Zi /Od /I "..\..\..\..\incl" /I "..\..\..\..\corba" /I "..\..\..\..\..\support\incl\nt" /D "WIN32" /D "NDEBUG" /D "_MBCS" /D "_LIB" /D "DEBUG" /YX /FD /c
# ADD CPP /nologo /MD /W3 /GR /GX /Zi /Od /I "..\..\..\incl" /I "..\..\..\corba" /I "..\..\..\..\support\incl\nt" /D "WIN32" /D "NDEBUG" /D "_MBCS" /D "_LIB" /D "DEBUG" /D "USE_STLPORT" /YX /FD /c
# ADD BASE RSC /l 0x407 /d "NDEBUG"
# ADD RSC /l 0x407 /d "NDEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LIB32=link.exe -lib
# ADD BASE LIB32 /nologo /out:"..\..\..\..\lib\mstBuilderLib.lib"
# ADD LIB32 /nologo /out:"..\..\..\lib\mstBuilderLibs.lib"

!ENDIF 

# Begin Target

# Name "mstBuilderLib - Win32 Release"
# Name "mstBuilderLib - Win32 Debug"
# Name "mstBuilderLib - Win32 Debug STLport"
# Name "mstBuilderLib - Win32 Release STLport"
# Begin Group "CORBASource"

# PROP Default_Filter "cpp;c;cxx;rc;def;r;odl;idl;hpj;bat"
# Begin Source File

SOURCE=.\mstBuilderC.cpp
# End Source File
# Begin Source File

SOURCE=.\mstBuilderS.cpp
# End Source File
# Begin Source File

SOURCE=.\mstBuilderS_T.cpp
# End Source File
# End Group
# Begin Group "CORBAHeader"

# PROP Default_Filter "h;hpp;hxx;hm;inl"
# Begin Source File

SOURCE=.\mstBuilderC.h
# End Source File
# Begin Source File

SOURCE=.\mstBuilderI.h
# End Source File
# Begin Source File

SOURCE=.\mstBuilderS.h
# End Source File
# Begin Source File

SOURCE=.\mstBuilderS_T.h
# End Source File
# End Group
# Begin Source File

SOURCE=.\makefile
# End Source File
# Begin Source File

SOURCE=.\mstBuilder.idl

!IF  "$(CFG)" == "mstBuilderLib - Win32 Release"

!ELSEIF  "$(CFG)" == "mstBuilderLib - Win32 Debug"

# Begin Custom Build
InputPath=.\mstBuilder.idl
InputName=mstBuilder

BuildCmds= \
	..\..\..\..\..\support\bin-nt386\tao_idl  -GI $(InputName).idl  -I ..\..\..\corba

"$(InputName)C.h" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"$(InputName)C.i" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"$(InputName)C.cpp" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"$(InputName)S.h" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"$(InputName)S.i" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"$(InputName)S.cpp" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"$(InputName)S_T.h" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"$(InputName)S_T.i" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"$(InputName)S_T.cpp" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)
# End Custom Build

!ELSEIF  "$(CFG)" == "mstBuilderLib - Win32 Debug STLport"

!ELSEIF  "$(CFG)" == "mstBuilderLib - Win32 Release STLport"

!ENDIF 

# End Source File
# Begin Source File

SOURCE=.\mstBuilderLib.h
# End Source File
# End Target
# End Project
