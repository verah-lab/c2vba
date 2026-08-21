#include <db_sys.h>
#include "SeqValue.h"

enum inf_range {
		rng_ea
	//,	rng_explea
	,	rng_node
	//,	rng_node_explea
	//,	rng_node_explea_da
	,	rng_de2cluster
	//,	rng_de2cluster_explea
	//,	rng_de2cluster_explea_da
	,	rng_cluster2de
	//,	rng_cluster2de_explea
	//,	rng_cluster2de_explea_da
};

class CommonInfo {
	public: 
		datakind *dk;
		inf_range range;
		Value *theExplicitEaid;
		CommonInfo *theExplicitDa;
		bool old:1;
		bool explicitEaid:1;
		bool explInDa:1;
		
		CommonInfo()
		{
			dk = 0;
			range = rng_ea;
			theExplicitEaid = 0;
			theExplicitDa = 0;
			old = true;
			explicitEaid = false;
			explInDa = false;
		}
};
