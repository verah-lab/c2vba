#----let global make know which targets are supported-----
all:
link:
clean:
#library:
#proto:
#---------------------------------------------------------

variants = release() debug(_d) 

GRM				=	sequencer
GRMCL			=	Sequencer

# erase standard flags:
mm_std_flags =

cxxflags = -fimplicit-templates -Wno-unused

# only PROJECT_CFLAGS are ordered BEFORE automatic -I$(INCLDIR)
# this is important here because some headers eclipse headers in $INCLDIR

PROJECT_CFLAGS = -I. -I../gdSndRcvTlsLib -I../gdSndTls

# the following files fail to compile with implicit-templates with gcc 2.8.1


binaries			=	gdSqu

libs                =   o2o${curVarExt}			\
                        ddpCfgIO${curVarExt}    \
						gdtls${curVarExt}       \
                        ddp${curVarExt}         \
                        hb${curVarExt}          \
                        hbComm${curVarExt}      \
                        pccts${curVarExt}		\
                        ${DBLIBS}               \
                        ${TCPLIBS}

gdSquObjects =              	\
                  DaInfo.o     	\
                  Impl.o       	\
                  parseDum1.o  	\
                  parseDum2.o  	\
                  seq.o 		\
                  seqLexer.o	\
                  SeqOp.o      	\
                  Sequencer.o  	\
                  sequencerm.o 	\
                  SeqValue.o   	\
                  Timer.o

gdSqu.link       =   ${DBLIBRARIES}

gdSqu.externalObjects= ${DBOBJ}

sequencer_flags = -Wno-return-type -Wno-unused -Wno-traditional -W -Wno-error

seq.cpp Sequencer.cpp seqLexer.h Sequencer.h: seq.g
	antlr -CC -k 2 -ge -k 4 -mrhoist on -prc on seq.g  
	dlg -CC -cl seqLexer parser.dlg  


include ${MMDIR}/stddepl

