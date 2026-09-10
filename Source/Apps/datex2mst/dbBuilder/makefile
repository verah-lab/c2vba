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


binaries = d2MSTPubDBBuilder

copyDestinations = bin
binDir = ${BINDIR}

variants = debug(_d) release() #debugInsure(di)

INCLDIR := ${INCLDIR4}/xercesc270 -I${INCLDIR}

cflags = -DSYSV -DPROTOTYPES -DMAP_IMPLEMENTATION -I${INCLDIR2}/DDL -DDEBUG\
		 -I${INCLDIR4}/xercesc270 -fimplicit-templates ${CORBAINCLS} -DUSE_PREFIX

LOCLIBS = loc_man_client${curVarExt} locServer${curVarExt} lm_api${curVarExt} loc_man${curVarExt} db_list${curVarExt} polyline${curVarExt}

HBLIBS = ddp${curVarExt} hbComm${curVarExt} hblibExt${curVarExt} hb${curVarExt}



libs   = ${LOCLIBS} d2MSTDbBuilderIface${curVarExt} hbCORBAExt${curVarExt} ${CORBALIBS} ${HBLIBS} ${DBLIBS} ${TCPLIBS} xerces-c_27


#.debug/handler.o .release/handler.o : gd_ddp_trails.h

#gd_ddp_trails.h : trails.ddl
#	ddl -parse -header -dic=/vobs/support/ddl-temp/dic -addIncl=$(BINDIR)/../ddl trails.ddl

#ddl = trails.ddl

d2MSTPubDBBuilder.objects	=\
		d2MSTPubDBBuilder.o\
		d2MSTDbBuilderIfaceImpl.o\
		handler.o\
        Value.o\
		ValueBase.o\
		VBase.o \
        ddpmap.o\
        ddpmapDDP.o \
        item.o \
        psError.o \
        psString.o \
        psTimeMan.o \
        baseXMLBase.o \
        xmlDefReader.o \
        loc_match.o \
		state.o

d2MSTPubDBBuilder.debug.link = ${INFORMIXLIBRARIES}

d2MSTPubDBBuilder.release.link = ${INFORMIXLIBRARIES}



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
