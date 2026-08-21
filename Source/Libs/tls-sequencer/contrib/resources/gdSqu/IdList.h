#ifndef I_IDLIST_H
#define I_IDLIST_H

#include <SEArrayT.h>

class IdList : public SEArray<long> {
	public:
		IdList() : dataType( -1 ) {
			reserve( 48 );
		}
		long dataType;
};

extern bool GetIdList (	long	eaid,
						long	ea_index,
						int		destspec,
						long	from_da,
						long	to_da,
						bool 	index_valid,
						short	cmd,
						IdList	&res );

#define GUELTIG_DE								1
#define GUELTIG_KNOTEN							2
#define GUELTIG_NORMALE_DES_AN_CLUSTER_DE	3
#define GUELTIG_CLUSTER_DE_AN_NORMALE_DES	4

#define EA_TEST 		1
#define EAID_GET  	2
#define EAINDX_GET	3

#endif
