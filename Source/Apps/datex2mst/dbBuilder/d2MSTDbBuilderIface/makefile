################################################################################
# targets supported ############################################################
all:
link:
library:
clean:
proto:
################################################################################

make_recursive = yes

IDL_COMPILER_FLAGS = -GT -si S.i -ci C.i

variants = release() debug(_d)

cxxflags = -g -frtti -DUSE_STL -fimplicit-templates ${CORBAINCLS} -I. 

release.cxx_post_flags = -DDEBUG -O3
debug.cxx_post_flags = -DDEBUG

idls = d2MSTDbBuilderIface

libraries =	d2MSTDbBuilderIface



d2MSTDbBuilderIface.objects	=\
	d2MSTDbBuilderIfaceC.o\
	d2MSTDbBuilderIfaceS.o


corba=\
	d2MSTDbBuilderIface.h\
    d2MSTDbBuilderIfaceC.h\
    d2MSTDbBuilderIfaceS.h\
    d2MSTDbBuilderIfaceS_T.h\
    d2MSTDbBuilderIfaceC.cpp\
    d2MSTDbBuilderIfaceS.cpp\
    d2MSTDbBuilderIfaceS_T.cpp\
    d2MSTDbBuilderIfaceC.i\
    d2MSTDbBuilderIfaceS.i\
    d2MSTDbBuilderIface.idl


    include ${MMDIR}/stddepl
