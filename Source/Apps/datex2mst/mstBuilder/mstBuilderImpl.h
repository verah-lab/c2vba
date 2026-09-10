// ******************************************************************************************
//
//	Author   : P. Schmitz/Heusch-Boesefeldt GmbH
//	Kommentar: 
//			   
//
// ******************************************************************************************
#ifndef _MSTDBBUILDERIMPL_H_
#define _MSTDBBUILDERIMPL_H_


#include "mstBuilderLib.h"
#include "CORBAObjectHandle.h"
#include "mbMapping.h"
using namespace mstBuilder;

#include "mbMst.h"
#include "mstRequest.h"

#include "ddp.h"
#include "loc_man.h"



///////////////////////////////////////////////////////////////////////////
// d2MSTDbBuilderIfaceImpl
///////////////////////////////////////////////////////////////////////////

class  mstBuilderImpl : public virtual POA_mstBuilder::Builder, public ddpCompoundQuery, private hbGuarded
{
public:
	mstBuilderImpl( ddpConnection* connection, 
		            const hbString& d2Version, 
					const hbString& geodyn2WLSAddress,
					bool dimsoecr,
					bool useLocNameAsMsId,
					bool carriagewayFromGlane,
					const hbString& lveBetriebsParamDatakind );

    virtual ::mstBuilder::mstDatakinds * getAllDatakinds()
			ACE_THROW_SPEC (( CORBA::SystemException, mstBuilder::mstBuilderException));
    
    virtual ::mstBuilder::mstInfos * getAllMST()
			ACE_THROW_SPEC (( CORBA::SystemException, mstBuilder::mstBuilderException));
    
    virtual void writeMST( const mstBuilder::mstDefinition & oneMST )
			ACE_THROW_SPEC (( CORBA::SystemException, mstBuilder::mstBuilderException ));
    
    virtual void deleteMST( const char * _mstName, const char * _version )
			ACE_THROW_SPEC (( CORBA::SystemException, mstBuilder::mstBuilderException ));
    
    virtual ::mstBuilder::mstBuilderState * getStatus( const char * mstName, const char * version )
			ACE_THROW_SPEC (( CORBA::SystemException, mstBuilder::mstBuilderException ));

	virtual void createMST( const char * mstId, 
						    const char * mstVersion, 
							const char * defVersion, 
							const char * refVersion,
							const mstBuilder::mstFilter& filter )
	ACE_THROW_SPEC (( CORBA::SystemException, mstBuilder::mstBuilderException ));

public:
	void buildNextMST();

private:
	int getCycleLengthZQL_Q( long eaid );
	int getCycleLength( long eaid );
	int getCycleLengthQ( long locId );
	int getCarriageway( long locId );
	void getLocIds( const hbString& objType, vector<long>& eaids );

	long qid2LocId( long qid );
	long getLocId( mbReferenceTypeEnum refType, long eaid );

	void  readLayer( const hbString& objType, list<long>& IDs );
	void  _readLocations( const hbString& objType, const list<long>& IDs );

	void  readLocations( const hbString& objType );
	void  filterRDSVersion( const hbString& objType, list<long>& IDs );

	bool getLocName( long locId, mbMSTLocation& mstLoc );
	bool getLocation( long locId, mbReferenceTypeEnum refType,
					  mbMSTLocation& mstLoc, hbString& errTxt );
	void getLocationCoord(  long locId, mbMSTLocation& mstLoc );
	bool getLocationQ( mbReferenceTypeEnum refType, const hbString& qNameMode, 
					   long locId, mbMSTLocation& mstLoc, hbString& errTxt );

	bool getQName( const hbString& version, const hbString& refVersion, 
				   const hbString& qNameMode,
				   long  locId, 
				   mbMSTLocation& mstLoc, hbString& errTxt );


	// Reads MSTs initially, exits on error
	void readMSTs();

	bool readMSTConfig( const hbString& d2Version, hbString& error );

	bool readMSTDefinition( const hbString& mstId, 
							const hbString& defVersion,
							mstDefinition& def,
							hbString& error );

	mstBuilder::mstDefinition createMSTDefinition( mstRequest request );

	bool checkMST( const mstBuilder::mstDefinition & oneMST, long& errCode, hbString& errTxt );

	// Creates mbMSTLocation, mbMSTSource and mbMSTItem objects
	void buildRecords( const mstBuilder::mstDefinition & oneMST );

	// Writes mbMSTLocation, mbMSTSource and mbMSTItem objects to database
	void saveMST( mstDefinition oneMST, 
				  const mbMSTLocations& locs, 
				  const mbMSTSources& sources, 
				  const mbMSTItems& items,
				  mstBuilder::state S );

	void saveState( const hbString& mstName, 
					const hbString& version,
				    const mstState& S );

	void updateState( const hbString& mstName, 
					  const hbString& version,
					  mstBuilder::state newState,
					  bool buildHasStarted,
					  const hbString& error );


private:
	// DDP-Query-Interface
	virtual void	callback(CallbackType type);
	virtual void	error( long errorCode, const char* errorText );

private:
	// dontIgnoreMSOutsideExtremeCoorRect 
	// true: GeoDyn objects with coordinates outside of the
	//       extreme coordinate rectangle (s. datakind extreme_coor) are 
	//       'accepted' as valid measurement sites. Both coordinate values 
	//       are set to -1.
	bool							m_dimsoecr;

	bool							m_useLocNameAsMsId;
	bool							m_carriagewayFromGlane;

private:
	map< long, mbMSTLocation >		m_currentLocs;
	mbMSTSources					m_currentSources;
	mbMSTItems						m_currentItems;

	unsigned						m_currentLocCount;
	unsigned						m_currentSrcCount;
	unsigned						m_currentItemCount;

	map< long, unsigned >			m_currentLocIndex;


	map< long, mbMSTLocation >		m_eaid2Loc;

	map< hbString, set<long> >		m_notLocMatchingEAIDs;

	map< long, long >				m_qid2LocId;


private:
	map< long, unsigned >			m_eaid2Cycle;

	ddpConnection*					m_connection;
	locConnection*					m_locConnection;

	hbString						m_lclVersion;

	ddpQuery*						m_lveBAQuery;
	ddpQuery*						m_ufdBAQuery;

	hbString						m_d2Version;
	mbDatakindMappings				m_datakindMappings;

	typedef map< long, locLocationInfoExt > locInfoMap; 
	map< hbString, locInfoMap >		m_locInfos;

	map< hbString, mstDefinition  >	m_msts;
	map< hbString, mstState >		m_mst2State;

	hbString						m_currentObjType;

	// MSTs to be processed
	list<mstRequest>				m_requests;
	hbString						m_currentMSTId;
	hbString						m_currentMSTDefVersion;
	hbString						m_currentMSTVersion;
	time_t							m_currentBuildStart;

	long							m_min_x;
	long							m_min_y;
	long							m_max_x;
	long							m_max_y;
};




#endif				/*	_D2MSTDBBUILDERIFACEIMPL_H_   */
