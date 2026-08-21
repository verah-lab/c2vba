#ifndef I_POOLDEF_H
#define I_POOLDEF_H

#include "LazyPool.h"
#include "BlockedPool.h"
#include "avlTree44.h"


#define NMPOOL(sz) SZ_Pool##sz

#define DEFPOOL(sz,num) static LazyPool NMPOOL(sz) (num,sz); \
	/*int _p_##sz =*/ _rememberPool (NMPOOL(sz), sz);

extern avlTree44<unsigned int, Pool*> *Pools;
extern "C" void cleanup_libs (int);

inline int _rememberPool (Pool &p, unsigned int sz)
{
	if ( Pools->insert (sz, &p) ) {
		fprintf ( stderr, "Kann mir Pool der Groesse %u nicht merken\n", sz );
		cleanup_libs (12);
		exit (12);
	}
	return (1);
}
inline LazyPool *EFNMPOOL (unsigned int sz)
{
	Pool *p;
	if ( ! Pools->find (sz, p ) ) return ((LazyPool *)p);
	fprintf ( stderr, "Kann Pool der Groesse %u nicht finden\n", sz );
	//cleanup_libs (12);
	//exit (12);
	return (0);
}
#define FNMPOOL(sz) 0

#endif
