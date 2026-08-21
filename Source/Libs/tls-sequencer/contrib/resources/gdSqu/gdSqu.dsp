# Microsoft Developer Studio Project File - Name="gdSqu" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 6.00
# ** DO NOT EDIT **

# TARGTYPE "Win32 (x86) Console Application" 0x0103

CFG=gdSqu - Win32 Debug
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "gdSqu.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "gdSqu.mak" CFG="gdSqu - Win32 Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "gdSqu - Win32 Debug" (based on "Win32 (x86) Console Application")
!MESSAGE "gdSqu - Win32 Release" (based on "Win32 (x86) Console Application")
!MESSAGE 

# Begin Project
# PROP AllowPerConfigDependencies 0
# PROP Scc_ProjName "gdSqu"
# PROP Scc_LocalPath "."
CPP=cl.exe
RSC=rc.exe

!IF  "$(CFG)" == "gdSqu - Win32 Debug"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir "gdSqu___Win32_Debug"
# PROP BASE Intermediate_Dir "gdSqu___Win32_Debug"
# PROP BASE Ignore_Export_Lib 0
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 1
# PROP Output_Dir "gdSqu___Win32_Debug"
# PROP Intermediate_Dir "gdSqu___Win32_Debug"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
MTL=midl.exe
# ADD BASE CPP /nologo /MDd /W3 /Gm /GR /GX /Zi /Od /I "." /I "../gdSndRcvTlsLib" /I "../../incl" /D "SEQUENCER" /D "PROTOTYPES" /D "PCCTS_USE_NAMESPACE_STD" /D "PURIFY(r" /D "s)" /D DONT_HAVE_INFORMIX=0 /D "PARSER_EXCEPTION" /D "RN_PRED_EXTENSION" /D ANTLRCommonTokenTEXTSIZE=512 /D "WIN32" /D "_DEBUG" /D "_CONSOLE" /D "_MBCS" /D "INFORMIX" /FD /GZ /c
# SUBTRACT BASE CPP /YX
# ADD CPP /nologo /MDd /W3 /Gm /GR /GX /Zi /Od /I "." /I "../gdSndRcvTlsLib" /I "../../incl" /D "SEQUENCER" /D "PROTOTYPES" /D "PCCTS_USE_NAMESPACE_STD" /D "PURIFY(r" /D "s)" /D DONT_HAVE_INFORMIX=0 /D "PARSER_EXCEPTION" /D "RN_PRED_EXTENSION" /D ANTLRCommonTokenTEXTSIZE=512 /D "WIN32" /D "_DEBUG" /D "_CONSOLE" /D "_MBCS" /FD /GZ /c
# SUBTRACT CPP /YX
# ADD BASE RSC /l 0x407 /d "_DEBUG"
# ADD RSC /l 0x407 /d "_DEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 pccts_d.lib gdSndRcvTlsLib_d.lib gdtls_d.lib dbiflib_9.21_d.obj /nologo /subsystem:console /pdb:"../../bin/inf/gdSqu_d.pdb" /debug /machine:I386 /out:"../../bin/inf/gdSqu_d.exe" /pdbtype:sept /libpath:"../../lib" /libpath:"..\..\..\support\lib\nt386\vc60"
# SUBTRACT BASE LINK32 /pdb:none
# ADD LINK32 pccts_d.lib gdSndRcvTlsLib_d.lib gdtls_d.lib /nologo /subsystem:console /pdb:"../../bin/gdSqu_d.pdb" /debug /machine:I386 /out:"../../bin/gdSqu_d.exe" /pdbtype:sept /libpath:"../../lib" /libpath:"..\..\..\support\lib\nt386\vc60"
# SUBTRACT LINK32 /pdb:none

!ELSEIF  "$(CFG)" == "gdSqu - Win32 Release"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir "gdSqu___Win32_Release"
# PROP BASE Intermediate_Dir "gdSqu___Win32_Release"
# PROP BASE Ignore_Export_Lib 0
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 0
# PROP Output_Dir "gdSqu___Win32_Release"
# PROP Intermediate_Dir "gdSqu___Win32_Release"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
MTL=midl.exe
# ADD BASE CPP /nologo /MD /W3 /GR /GX /Zi /O2 /I "." /I "../gdSndRcvTlsLib" /I "../../incl" /D "SEQUENCER" /D "PROTOTYPES" /D "PCCTS_USE_NAMESPACE_STD" /D "PURIFY(r" /D "s)" /D DONT_HAVE_INFORMIX=0 /D "PARSER_EXCEPTION" /D "RN_PRED_EXTENSION" /D ANTLRCommonTokenTEXTSIZE=512 /D "INFORMIX" /D "WIN32" /D "NDEBUG" /D "_CONSOLE" /D "_MBCS" /YX /FD /c
# ADD CPP /nologo /MD /W3 /GR /GX /Zi /O2 /I "." /I "../gdSndRcvTlsLib" /I "../../incl" /D "SEQUENCER" /D "PROTOTYPES" /D "PCCTS_USE_NAMESPACE_STD" /D "PURIFY(r" /D "s)" /D DONT_HAVE_INFORMIX=0 /D "PARSER_EXCEPTION" /D "RN_PRED_EXTENSION" /D ANTLRCommonTokenTEXTSIZE=512 /D "WIN32" /D "NDEBUG" /D "_CONSOLE" /D "_MBCS" /YX /FD /c
# ADD BASE RSC /l 0x407 /d "NDEBUG"
# ADD RSC /l 0x407 /d "NDEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 pccts.lib gdSndRcvTlsLib.lib gdtls.lib dbiflib_9.21.obj /nologo /subsystem:console /pdb:"../../bin/inf/gdSqu.pdb" /debug /machine:I386 /out:"../../bin/inf/gdSqu.exe" /pdbtype:sept /libpath:"../../lib" /libpath:"..\..\..\support\lib\nt386\vc60"
# SUBTRACT BASE LINK32 /pdb:none
# ADD LINK32 pccts.lib gdSndRcvTlsLib.lib gdtls.lib /nologo /subsystem:console /pdb:"../../bin/gdSqu.pdb" /debug /machine:I386 /out:"../../bin/gdSqu.exe" /pdbtype:sept /libpath:"../../lib" /libpath:"..\..\..\support\lib\nt386\vc60"
# SUBTRACT LINK32 /pdb:none

!ENDIF 

# Begin Target

# Name "gdSqu - Win32 Debug"
# Name "gdSqu - Win32 Release"
# Begin Group "Source Files"

# PROP Default_Filter "cpp;c;cxx;rc;def;r;odl;idl;hpj;bat"
# Begin Source File

SOURCE=.\DaInfo.cpp
# End Source File
# Begin Source File

SOURCE=.\Impl.cpp
# End Source File
# Begin Source File

SOURCE=.\parseDum1.cpp
# End Source File
# Begin Source File

SOURCE=.\parseDum2.cpp
# End Source File
# Begin Source File

SOURCE=.\seq.cpp
# End Source File
# Begin Source File

SOURCE=.\seq.g

!IF  "$(CFG)" == "gdSqu - Win32 Debug"

# Begin Custom Build
InputPath=.\seq.g

BuildCmds= \
	ANTLR -CC -k 2 -ge -k 4 -mrhoist on -prc on $(InputPath) \
	DLG -CC -cl seqLexer parser.dlg \
	;rem LnDirBsl2Sl_d Sit2TxtTxtTemplateParserG.cpp \
	;rem del Sit2TxtTxtTemplateParserG.cpp \
	;rem ren .bsl2sl Sit2TxtTxtTemplateParserG.cpp \
	;rem LnDirBsl2Sl_d Sit2TxtTxtTemplateParser.h \
	;rem del Sit2TxtTxtTemplateParser.h \
	;rem ren .bsl2sl Sit2TxtTxtTemplateParser.h \
	

"tokens.h" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"seq.cpp" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"seqLexer.h" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"seqLexer.cpp" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"Sequencer.cpp" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"Sequencer.h" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)
# End Custom Build

!ELSEIF  "$(CFG)" == "gdSqu - Win32 Release"

# Begin Custom Build
InputPath=.\seq.g

BuildCmds= \
	ANTLR -CC -k 2 -ge -k 4 -mrhoist on -prc on $(InputPath) \
	DLG -CC -cl seqLexer parser.dlg \
	;rem LnDirBsl2Sl_d Sit2TxtTxtTemplateParserG.cpp \
	;rem del Sit2TxtTxtTemplateParserG.cpp \
	;rem ren .bsl2sl Sit2TxtTxtTemplateParserG.cpp \
	;rem LnDirBsl2Sl_d Sit2TxtTxtTemplateParser.h \
	;rem del Sit2TxtTxtTemplateParser.h \
	;rem ren .bsl2sl Sit2TxtTxtTemplateParser.h \
	

"tokens.h" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"seq.cpp" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"seqLexer.h" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"seqLexer.cpp" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"Sequencer.cpp" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"Sequencer.h" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)
# End Custom Build

!ENDIF 

# End Source File
# Begin Source File

SOURCE=.\seqLexer.cpp
# End Source File
# Begin Source File

SOURCE=.\SeqOp.cpp
# End Source File
# Begin Source File

SOURCE=.\Sequencer.cpp
# End Source File
# Begin Source File

SOURCE=.\sequencerm.cpp
# End Source File
# Begin Source File

SOURCE=.\SeqValue.cpp
# End Source File
# Begin Source File

SOURCE=.\Timer.cpp
# End Source File
# End Group
# Begin Group "Header Files"

# PROP Default_Filter "h;hpp;hxx;hm;inl"
# Begin Source File

SOURCE=.\CommonInfo.h
# End Source File
# Begin Source File

SOURCE=.\CurEnv.h
# End Source File
# Begin Source File

SOURCE=.\DaInfo.h
# End Source File
# Begin Source File

SOURCE=.\DbgAnseq.h
# End Source File
# Begin Source File

SOURCE=.\IdList.h
# End Source File
# Begin Source File

SOURCE=.\nseqDebug.h
# End Source File
# Begin Source File

SOURCE=.\ParseIntern.h
# End Source File
# Begin Source File

SOURCE=.\PoolDef.h
# End Source File
# Begin Source File

SOURCE=.\rnIdent.h
# End Source File
# Begin Source File

SOURCE=.\seqLexer.h
# End Source File
# Begin Source File

SOURCE=.\SeqOp.h
# End Source File
# Begin Source File

SOURCE=.\Sequencer.h
# End Source File
# Begin Source File

SOURCE=.\SeqValue.h
# End Source File
# Begin Source File

SOURCE=.\StackTmpl.h
# End Source File
# Begin Source File

SOURCE=.\Timer.h
# End Source File
# Begin Source File

SOURCE=.\ValueList.h
# End Source File
# Begin Source File

SOURCE=.\ZD.h
# End Source File
# End Group
# Begin Group "Resource Files"

# PROP Default_Filter "ico;cur;bmp;dlg;rc2;rct;bin;rgs;gif;jpg;jpeg;jpe"
# End Group
# Begin Source File

SOURCE=.\args.txt
# End Source File
# Begin Source File

SOURCE=.\gdSqu.mak
# End Source File
# Begin Source File

SOURCE=.\makefile
# End Source File
# Begin Source File

SOURCE=.\testinput
# End Source File
# End Target
# End Project
