###########################################################################
#
# Makefile fuer die neue gemeinsame Produktionsumgebug mit Ronalds makefile
#
# Voraussetzungen : 
#
# Die Shellvariablen
#
###########################################################################
#
#
#----let global make know which targets are supported-----
all:
proto:
#link:
clean:

###########################################################################
#
#
############# Flags:

#make_recursive = yes


binaries = mstBuilderSrv

copyDestinations = bin
binDir = ${BINDIR}

variants = debug(_d) release() #debugInsure(di)

INCLDIR := ${INCLDIR4}/xercesc270 -I${INCLDIR}

cflags = -DSYSV -DPROTOTYPES -DMAP_IMPLEMENTATION -I${INCLDIR2}/DDL -DDEBUG\
		 -I${INCLDIR4}/xercesc270 -fimplicit-templates ${CORBAINCLS} -DUSE_PREFIX

LOCLIBS = loc_man_client${curVarExt} locServer${curVarExt} lm_api${curVarExt} loc_man${curVarExt} db_list${curVarExt} polyline${curVarExt}

HBLIBS = ddp${curVarExt} hbComm${curVarExt} hblibExt${curVarExt} hb${curVarExt}



libs   = ${LOCLIBS} mstBuilderLib${curVarExt} ddpCfgIO${curVarExt} \
         hbCORBAExt${curVarExt} ${CORBALIBS} ${HBLIBS} ${DBLIBS} ${TCPLIBS} xerces-c_27



mstBuilderSrv.objects	=\
		mstBuilderSrv.o\
		mstBuilderImpl.o\
        mbMapping.o \
        baseXMLBase.o \
        xmlDefReader.o \
        loc_match.o


mstBuilderSrv.debug.link = ${INFORMIXLIBRARIES}

mstBuilderSrv.release.link = ${INFORMIXLIBRARIES}



include ${MMDIR}/stddepl

ifdef variant.release
cflags += -DDEBUG -g
endif

ifdef variant.debug
cflags += -DDEBUG -g
endif

ifdef variant.debugInsure
cflags += -DDEBUG -g
CC=insight
CXX=insight
endif
