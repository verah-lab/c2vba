# Microsoft Developer Studio Generated NMAKE File, Based on gdSqu.dsp
!IF "$(CFG)" == ""
CFG=gdSqu - Win32 Debug
!MESSAGE No configuration specified. Defaulting to gdSqu - Win32 Debug.
!ENDIF 

!IF "$(CFG)" != "gdSqu - Win32 Debug" && "$(CFG)" != "gdSqu - Win32 Release"
!MESSAGE Invalid configuration "$(CFG)" specified.
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
!ERROR An invalid configuration is specified.
!ENDIF 

!IF "$(OS)" == "Windows_NT"
NULL=
!ELSE 
NULL=nul
!ENDIF 

CPP=cl.exe
RSC=rc.exe

!IF  "$(CFG)" == "gdSqu - Win32 Debug"

OUTDIR=.\gdSqu___Win32_Debug
INTDIR=.\gdSqu___Win32_Debug

ALL : ".\tokens.h" ".\Sequencer.h" ".\Sequencer.cpp" ".\seqLexer.h" ".\seqLexer.cpp" ".\seq.cpp" "..\..\bin\gdSqu_d.exe"


CLEAN :
	-@erase "$(INTDIR)\DaInfo.obj"
	-@erase "$(INTDIR)\Impl.obj"
	-@erase "$(INTDIR)\parseDum1.obj"
	-@erase "$(INTDIR)\parseDum2.obj"
	-@erase "$(INTDIR)\seq.obj"
	-@erase "$(INTDIR)\seqLexer.obj"
	-@erase "$(INTDIR)\SeqOp.obj"
	-@erase "$(INTDIR)\Sequencer.obj"
	-@erase "$(INTDIR)\sequencerm.obj"
	-@erase "$(INTDIR)\SeqValue.obj"
	-@erase "$(INTDIR)\Timer.obj"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(INTDIR)\vc60.pdb"
	-@erase "..\..\bin\gdSqu_d.exe"
	-@erase "..\..\bin\gdSqu_d.ilk"
	-@erase "..\..\bin\gdSqu_d.pdb"
	-@erase "seq.cpp"
	-@erase "seqLexer.cpp"
	-@erase "seqLexer.h"
	-@erase "Sequencer.cpp"
	-@erase "Sequencer.h"
	-@erase "tokens.h"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

MTL=midl.exe
CPP_PROJ=/nologo /MDd /W3 /Gm /GR /GX /Zi /Od /I "." /I "../gdSndRcvTlsLib" /I "../../incl" /D "SEQUENCER" /D "PROTOTYPES" /D "PCCTS_USE_NAMESPACE_STD" /D "PURIFY(r" /D "s)" /D DONT_HAVE_INFORMIX=0 /D "PARSER_EXCEPTION" /D "RN_PRED_EXTENSION" /D ANTLRCommonTokenTEXTSIZE=512 /D "WIN32" /D "_DEBUG" /D "_CONSOLE" /D "_MBCS" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /GZ /c 
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\gdSqu.bsc" 
BSC32_SBRS= \
	
LINK32=link.exe
LINK32_FLAGS=pccts_d.lib gdSndRcvTlsLib_d.lib gdtls_d.lib /nologo /subsystem:console /incremental:yes /pdb:"../../bin/gdSqu_d.pdb" /debug /machine:I386 /out:"../../bin/gdSqu_d.exe" /pdbtype:sept /libpath:"../../lib" /libpath:"..\..\..\support\lib\nt386\vc60" 
LINK32_OBJS= \
	"$(INTDIR)\DaInfo.obj" \
	"$(INTDIR)\Impl.obj" \
	"$(INTDIR)\parseDum1.obj" \
	"$(INTDIR)\parseDum2.obj" \
	"$(INTDIR)\seq.obj" \
	"$(INTDIR)\seqLexer.obj" \
	"$(INTDIR)\SeqOp.obj" \
	"$(INTDIR)\Sequencer.obj" \
	"$(INTDIR)\sequencerm.obj" \
	"$(INTDIR)\SeqValue.obj" \
	"$(INTDIR)\Timer.obj"

"..\..\bin\gdSqu_d.exe" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_OBJS)
<<

!ELSEIF  "$(CFG)" == "gdSqu - Win32 Release"

OUTDIR=.\gdSqu___Win32_Release
INTDIR=.\gdSqu___Win32_Release

ALL : ".\tokens.h" ".\Sequencer.h" ".\Sequencer.cpp" ".\seqLexer.h" ".\seqLexer.cpp" ".\seq.cpp" "..\..\bin\gdSqu.exe"


CLEAN :
	-@erase "$(INTDIR)\DaInfo.obj"
	-@erase "$(INTDIR)\Impl.obj"
	-@erase "$(INTDIR)\parseDum1.obj"
	-@erase "$(INTDIR)\parseDum2.obj"
	-@erase "$(INTDIR)\seq.obj"
	-@erase "$(INTDIR)\seqLexer.obj"
	-@erase "$(INTDIR)\SeqOp.obj"
	-@erase "$(INTDIR)\Sequencer.obj"
	-@erase "$(INTDIR)\sequencerm.obj"
	-@erase "$(INTDIR)\SeqValue.obj"
	-@erase "$(INTDIR)\Timer.obj"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(INTDIR)\vc60.pdb"
	-@erase "..\..\bin\gdSqu.exe"
	-@erase "..\..\bin\gdSqu.pdb"
	-@erase "seq.cpp"
	-@erase "seqLexer.cpp"
	-@erase "seqLexer.h"
	-@erase "Sequencer.cpp"
	-@erase "Sequencer.h"
	-@erase "tokens.h"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

MTL=midl.exe
CPP_PROJ=/nologo /MD /W3 /GR /GX /Zi /O2 /I "." /I "../gdSndRcvTlsLib" /I "../../incl" /D "SEQUENCER" /D "PROTOTYPES" /D "PCCTS_USE_NAMESPACE_STD" /D "PURIFY(r" /D "s)" /D DONT_HAVE_INFORMIX=0 /D "PARSER_EXCEPTION" /D "RN_PRED_EXTENSION" /D ANTLRCommonTokenTEXTSIZE=512 /D "WIN32" /D "NDEBUG" /D "_CONSOLE" /D "_MBCS" /Fp"$(INTDIR)\gdSqu.pch" /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\gdSqu.bsc" 
BSC32_SBRS= \
	
LINK32=link.exe
LINK32_FLAGS=pccts.lib gdSndRcvTlsLib.lib gdtls.lib /nologo /subsystem:console /incremental:no /pdb:"../../bin/gdSqu.pdb" /debug /machine:I386 /out:"../../bin/gdSqu.exe" /pdbtype:sept /libpath:"../../lib" /libpath:"..\..\..\support\lib\nt386\vc60" 
LINK32_OBJS= \
	"$(INTDIR)\DaInfo.obj" \
	"$(INTDIR)\Impl.obj" \
	"$(INTDIR)\parseDum1.obj" \
	"$(INTDIR)\parseDum2.obj" \
	"$(INTDIR)\seq.obj" \
	"$(INTDIR)\seqLexer.obj" \
	"$(INTDIR)\SeqOp.obj" \
	"$(INTDIR)\Sequencer.obj" \
	"$(INTDIR)\sequencerm.obj" \
	"$(INTDIR)\SeqValue.obj" \
	"$(INTDIR)\Timer.obj"

"..\..\bin\gdSqu.exe" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_OBJS)
<<

!ENDIF 

.c{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cpp{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cxx{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.c{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cpp{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cxx{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<


!IF "$(NO_EXTERNAL_DEPS)" != "1"
!IF EXISTS("gdSqu.dep")
!INCLUDE "gdSqu.dep"
!ELSE 
!MESSAGE Warning: cannot find "gdSqu.dep"
!ENDIF 
!ENDIF 


!IF "$(CFG)" == "gdSqu - Win32 Debug" || "$(CFG)" == "gdSqu - Win32 Release"
SOURCE=.\DaInfo.cpp

"$(INTDIR)\DaInfo.obj" : $(SOURCE) "$(INTDIR)"


SOURCE=.\Impl.cpp

"$(INTDIR)\Impl.obj" : $(SOURCE) "$(INTDIR)"


SOURCE=.\parseDum1.cpp

"$(INTDIR)\parseDum1.obj" : $(SOURCE) "$(INTDIR)"


SOURCE=.\parseDum2.cpp

"$(INTDIR)\parseDum2.obj" : $(SOURCE) "$(INTDIR)"


SOURCE=.\seq.cpp

"$(INTDIR)\seq.obj" : $(SOURCE) "$(INTDIR)"


SOURCE=.\seq.g

!IF  "$(CFG)" == "gdSqu - Win32 Debug"

InputPath=.\seq.g

".\tokens.h"	".\seq.cpp"	".\seqLexer.h"	".\seqLexer.cpp"	".\Sequencer.cpp"	".\Sequencer.h" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
	<<tempfile.bat 
	@echo off 
	ANTLR -CC -k 2 -ge -k 4 -mrhoist on -prc on $(InputPath) 
	DLG -CC -cl seqLexer parser.dlg 
	;rem LnDirBsl2Sl_d Sit2TxtTxtTemplateParserG.cpp 
	;rem del Sit2TxtTxtTemplateParserG.cpp 
	;rem ren .bsl2sl Sit2TxtTxtTemplateParserG.cpp 
	;rem LnDirBsl2Sl_d Sit2TxtTxtTemplateParser.h 
	;rem del Sit2TxtTxtTemplateParser.h 
	;rem ren .bsl2sl Sit2TxtTxtTemplateParser.h
<< 
	

!ELSEIF  "$(CFG)" == "gdSqu - Win32 Release"

InputPath=.\seq.g

".\tokens.h"	".\seq.cpp"	".\seqLexer.h"	".\seqLexer.cpp"	".\Sequencer.cpp"	".\Sequencer.h" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
	<<tempfile.bat 
	@echo off 
	ANTLR -CC -k 2 -ge -k 4 -mrhoist on -prc on $(InputPath) 
	DLG -CC -cl seqLexer parser.dlg 
	;rem LnDirBsl2Sl_d Sit2TxtTxtTemplateParserG.cpp 
	;rem del Sit2TxtTxtTemplateParserG.cpp 
	;rem ren .bsl2sl Sit2TxtTxtTemplateParserG.cpp 
	;rem LnDirBsl2Sl_d Sit2TxtTxtTemplateParser.h 
	;rem del Sit2TxtTxtTemplateParser.h 
	;rem ren .bsl2sl Sit2TxtTxtTemplateParser.h
<< 
	

!ENDIF 

SOURCE=.\seqLexer.cpp

"$(INTDIR)\seqLexer.obj" : $(SOURCE) "$(INTDIR)"


SOURCE=.\SeqOp.cpp

"$(INTDIR)\SeqOp.obj" : $(SOURCE) "$(INTDIR)"


SOURCE=.\Sequencer.cpp

"$(INTDIR)\Sequencer.obj" : $(SOURCE) "$(INTDIR)"


SOURCE=.\sequencerm.cpp

"$(INTDIR)\sequencerm.obj" : $(SOURCE) "$(INTDIR)"


SOURCE=.\SeqValue.cpp

"$(INTDIR)\SeqValue.obj" : $(SOURCE) "$(INTDIR)"


SOURCE=.\Timer.cpp

"$(INTDIR)\Timer.obj" : $(SOURCE) "$(INTDIR)"



!ENDIF 

