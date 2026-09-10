// ******************************************************************************************
//
//	Author   : P. Schmitz/Heusch-Boesefeldt GmbH
//	Kommentar: 
//			   
//
// ******************************************************************************************
#ifndef _D2MSTDBBUILDERIFACEIMPL_H_
#define _D2MSTDBBUILDERIFACEIMPL_H_


#include "d2MSTDbBuilderIface.h"
#include "CORBAObjectHandle.h"


//class buildObserver : public d2MSTDbBuilderIface::Observer {};
//typedef CORBAObjectHandle< buildObserver > observerHandle;
//typedef list< observerHandle > observerLst;


typedef hbString mstDef;
typedef list< mstDef > mstDefLst;
typedef list< hbString > mstIdLst;
typedef set< hbString > mstIdSet;

///////////////////////////////////////////////////////////////////////////
// d2MSTDbBuilderIfaceImpl
///////////////////////////////////////////////////////////////////////////

class  d2MSTDbBuilderIfaceImpl : public virtual POA_d2MSTDbBuilderIface::Server, private hbGuarded
{
public:
	// Empfängt Meldung anderer System-Instanzen
	virtual void createMST( const char * mstDef )
			throw ( CORBA::SystemException, d2MSTDbBuilderIface::Error );

	// Löscht MST
	virtual void deleteMST( const char * mstId )
			throw ( CORBA::SystemException, d2MSTDbBuilderIface::Error );

	bool getNextDef( mstDef& def );

	void delMSTs();

private:
	mstIdLst		m_mstIds;
	mstDefLst		m_mstDefs;
	mstIdSet		m_delMstIds;
};




#endif				/*	_D2MSTDBBUILDERIFACEIMPL_H_   */
