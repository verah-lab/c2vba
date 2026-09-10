################################################################################
# targets supported ############################################################
all:
link:library:
clean:
proto:
################################################################################
make_recursive = yes

IDL_COMPILER_FLAGS = -GT -si S.i -ci C.i
variants = release() debug(_d)
cxxflags = -g -frtti -DUSE_STL -fimplicit-templates ${CORBAINCLS} -I. 
release.cxx_post_flags = -DDEBUG -O3
debug.cxx_post_flags = -DDEBUG
idls = mstBuilder

libraries =	mstBuilderLib

mstBuilderLib.objects	=\
	mstBuilderC.o\
	mstBuilderS.o

corba=\
	mstBuilderLib.h\
    mstBuilderC.h\
    mstBuilderS.h\
    mstBuilderS_T.h\
    mstBuilderC.cpp\    mstBuilderS.cpp\
    mstBuilderS_T.cpp\
    mstBuilderC.i\
    mstBuilderS.i\
    mstBuilder.idl
    include ${MMDIR}/stddepl
