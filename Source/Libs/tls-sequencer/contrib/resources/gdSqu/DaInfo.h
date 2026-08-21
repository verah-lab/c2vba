#ifndef I_DAINFO_H
#define I_DAINFO_H

#include <SEArrayT.h>
#include <zdfacctypes.h>

#ifndef DB_IS_ZDF
#include "ddp.h"
#endif

class ZdfPartRequest;
class TransOp;
class rnBitArray2;

struct DaInfo {
	static SEArray<DaInfo*> writtenDAs;
	
	long datatype;
	SEArray<TransOp*> actions;
	rnBitArray2 *modified;
	unsigned char *data;
	long lastReceived;
	unsigned long unitsize;
	unsigned char *wrData;
	short shmblock;
	bool isTrigger;
	bool wasWritten;
	bool first;
#ifndef DB_IS_ZDF
	ddpDatakind ddpDa;
	long atime; // 'ZDF'-Zeit, mit der die Daten eingegangen sind. Wird beim Versenden benutzt
#endif
	
#ifdef DB_IS_ZDF
	DaInfo( long da );
#else
	DaInfo( long da, ddpConnection *con );
#endif
	~DaInfo () {}

	private:
	DaInfo ();
	DaInfo (const DaInfo&);
	DaInfo & operator = (const DaInfo&);
	public:

	void 				written( unsigned long indx );
#ifdef DB_IS_ZDF
	ZdfPartRequest *login();
	ZdfPartRequest	*sendData();
#else
	void	login();
	void	sendData( ddpTransaction &trans );
#endif
	unsigned char	*getBase()
		{ if (shmblock>=0) return (data); else return (0);}
	void				receive( DAREAD* );
	void				takeNewData( DAREAD* );
	void				cleanUp();

	static void		loginAll();
	static void		work();
	static void		sendAll();
};

#endif
