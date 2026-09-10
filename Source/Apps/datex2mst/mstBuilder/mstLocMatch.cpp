#ifdef WIN32
#pragma warning (disable : 4786)
#endif


#include "dbg_area_def.h"


extern "C" {

#ifdef DEBUGAREA
#undef DEBUGAREA
#endif
#define DEBUGAREA 1

#ifdef LOGGINGAREA
#undef LOGGINGAREA
#endif
#define LOGGINGAREA 1


#ifdef DEBUG
#undef DEBUG
#endif
#define DEBUG
#include "rnzdebug.h"
}


#include "LogBase.h"


#include "mstLocMatch.h"
#include "loc_man.h"
#include "ddpZdfQuery.h"

#include "gd_ddp_gd_loc_man.h"
//#include "gd_ddp_gd_road_net.h"


#define MB_ERR_EVAL_PMEASURE_FILTER 1017 



mstLocMatch* mstLocMatch::_instance = 0;
locConnection* mstLocMatch::m_locConnection = 0;


bool mstLocMatch::init( const mstObjFilters& objFilters, 
					  const mstRoadFilters& roadFilters,
					  hbString& errTxt )
{
	LogMethod1( "mstLocMatch", "init" );

	m_locClass = "";
	m_matchingLocIds.clear();
	m_notMatchingLocIds.clear();
	try
	{
		m_locList.clear();

		unsigned i;
		for( i = 0; i < roadFilters.length(); i++ )
		{
			const mstRoadFilter& rf = roadFilters[i];
			hbString road( (const char *)rf.name );

			long drType = m_locConnection->ddpConnect().datakind( "tr_directed_road" ).id();
			// long drType = ddp_tr_directed_road::Id();

			// @@@ Offsets berücksichtigen

			locLayer loc_layer( *m_locConnection , drType );

			list<long> loc_list;
			loc_layer.getLocations( loc_list );

			bool hasLoc = false;
			list<long>::const_iterator it;
			for( it = loc_list.begin(); it != loc_list.end() ; ++it )
			{
				locLocation drLoc( *m_locConnection , *it );
				LogF2( "Location Name: " << drLoc.name() );

				if ( drLoc.name() == road )
				{
					if( ( rf.startOffset != NO_OFFSET ) && ( rf.endOffset != NO_OFFSET ) )
					{
						locLocation fractionLoc = locLocation::NewFraction( *m_locConnection, drLoc, rf.startOffset, rf.endOffset  );
						m_locList.push_back( fractionLoc );
					}
					else
						m_locList.push_back( drLoc );

					hasLoc = true;
					break;
				}
			}

			if( !hasLoc )
			{
				errTxt = "Road '" + road + "' kann nicht instanziiert werden";
				HbErr( errTxt )

				return false;
			}
		}

		if( m_locList.size() )
		{
			m_roadLoc = locLocation::NewAggregation( *m_locConnection, m_locList );
		}

		m_objFilters = objFilters;

		unsigned j;
		for( j = 0; j < m_objFilters.length(); j++ )
		{
			if( m_objFilters[j].excluded )
			{
				if( hbString( m_objFilters[j].referenceType ) == "TLS" )
				{
					if( hbString( m_objFilters[j].type ) == "PMEASURE" )
					{
						hbString pmeasureId = m_objFilters[j].id;
						hbString pmeasureName = m_objFilters[j].name;

						// Find matching Qs

						unsigned char readFail;

						try
						{
							long pmId = -1;

							if( pmeasureId.empty() )
							{
								list<ddpObject> pmLst;
								readFail = ZdfPartRequest()
														.Space(ZdfPartSpace("PMEASURE", "PMEASURE"))
														.Read(pmLst);

								if( readFail )
								{
									mstBuilderException ex;
									ex.code = MB_ERR_EVAL_PMEASURE_FILTER;
									ex.description = hbString( "ZdfPartRequest read failed").c_str();
									ex.source = hbString("::getQsFromPMeasure()").c_str();

									throw ex;
								}

								list<ddpObject>::iterator itpm; 
								for( itpm = pmLst.begin(); itpm != pmLst.end(); ++itpm )
								{
									hbString name = (*itpm)["msrname"];
									if( name == pmeasureName )
										pmId = (*itpm)["msrid"];
								}

								if( pmId == -1 )
								{
									mstBuilderException ex;
									ex.code = MB_ERR_EVAL_PMEASURE_FILTER;
									ex.description = hbString( "ZdfPartRequest read failed: no ID for pmeasure <" ) + hbString(m_objFilters[j].id) + hbString(">") .c_str();
									ex.source = hbString("::getQsFromPMeasure()").c_str();

									throw ex;
								}
							}
							else
							{
								pmId = strtol( pmeasureId.c_str(), 0, 10 );
								if( pmId == 0 )
								{
									mstBuilderException ex;
									ex.code = MB_ERR_EVAL_PMEASURE_FILTER;
									ex.description = hbString( "Not a numerical ID <" ) + pmeasureId + hbString("> !") .c_str();
									ex.source = hbString("::getQsFromPMeasure()").c_str();

									throw ex;
								}

							}

							list<ddpObject> qLst;
							readFail = ZdfPartRequest().Space( ZdfPartSpace( "PMEASURE", pmId, "Q" ) )
								                     .Read( qLst );

							if( readFail )
							{
								mstBuilderException ex;
								ex.code = MB_ERR_EVAL_PMEASURE_FILTER;
								ex.description = hbString( "ZdfPartRequest read failed").c_str();
								ex.source = hbString("::getQsFromPMeasure()").c_str();

								throw ex;
							}

							cLog << "Ausschliessender Filter aufgeloest:" << endl;
							cLog << "Referenz-Typ:   " << m_objFilters[j].referenceType << endl;
							cLog << "Typ:            " << m_objFilters[j].type << endl;
							cLog << "ID:             " << m_objFilters[j].id << endl;
							cLog << "Name:           " << m_objFilters[j].name << endl;
							cLog << "Sub-IDs: " << endl;

							list<ddpObject>::iterator itl; 
							for( itl = qLst.begin(); itl != qLst.end(); ++itl )
							{
								long id = (*itl)["qid"].asLong();
								m_notMatchingLocIds.insert( id );

								cLog << "      Q-ID: " << id << endl;
							}
						}
						catch( const hbException& _ex )
						{
							mstBuilderException ex;
							ex.code = MB_ERR_EVAL_PMEASURE_FILTER;
							ex.description = ( hbString( "ZdfPartRequest read failed\n") + _ex.what() ).c_str();
							ex.source = hbString("::getQsFromPMeasure()").c_str();

							throw ex;
						}

					}
				}
			}
			else
				m_objNameSet.insert( hbString( m_objFilters[j].id ) );
		}
	}    
	catch( const hbException& ex )
	{
		errTxt = "Fehler in mstLocMatch::init()\n" + hbString( ex.what() );
		HbErr( errTxt )

		return false;
	}

	return true;
}

bool mstLocMatch::initLocClass( const hbString& locClass,
							  hbString& errTxt )
{
	if( m_locClass == locClass )
		return true;

	m_locClass = locClass;
	if( locClass.empty() )
	{
		m_matchingLocIds.clear();
		return true;
	}

	try
	{
		if( m_locList.size() )
		{
			long class_id = m_locConnection->ddpConnect().datakind( locClass ).id();

			list< locMatch > match_lst;
			m_roadLoc.getMatchingLocations( class_id, match_lst );

			list< locMatch >::iterator it; 
			for( it = match_lst.begin(); it != match_lst.end(); ++it )
				m_matchingLocIds.insert( it->id() );
		}
	}
	catch( const hbException& ex )
	{
		errTxt = "Fehler in mstLocMatch::iinitLocClassnit()\n" + hbString( ex.what() );
		HbErr( errTxt )

		return false;
	}

	return true;
}
 

bool mstLocMatch::match( long id, const hbString& locName )
{
	if( m_matchingLocIds.size() != 0 )
		return ( m_matchingLocIds.find( id ) != m_matchingLocIds.end() );

	if( m_notMatchingLocIds.find( id ) != m_notMatchingLocIds.end() )
		return false;

	try
	{
		if( !m_locList.size() && !m_objNameSet.size() )
			return true;

		if( m_locList.size() )
		{
			locLocation::MatchType t = m_roadLoc.match( id );     

			if( ( t != locLocation::Unknown ) && ( t != locLocation::NoMatch ) )
				return true;
		}

		if( m_objNameSet.size() )
		{
			if( m_objNameSet.find( locName ) != m_objNameSet.end() )
				return true;
		}

		m_notMatchingLocIds.insert( id );
		return false;
	}
	catch( const hbException& ex )
	{
		cerr << "!!! error in mstLocMatch::match() !!!" << endl;
		cerr << ex << endl;

		m_notMatchingLocIds.insert( id );
		return false;
	}
}


bool mstLocMatch::matchName( const hbString& locName )
{
	if( m_objNameSet.size() )
		return ( m_objNameSet.find( locName ) != m_objNameSet.end() );
	else
		return true;
}


