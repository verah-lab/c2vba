#ifdef WIN32
#pragma warning (disable : 4786)
#endif


#include "tao/ORB.h"
#include "trMisc.h"
#include "tr_err.h"



#include "handler.h"
#include "handler_l.h"
#include "ddp.h"
#include "gd_ddp_gd_loc_man.h"


#include "loc_man.h"
#include "psString.h"
#include "item.h"

#include <stdlib.h>
#include <iostream>
#include <fstream>

#include "ddpmapDDP.h"

#include "loc_match.h"
#include "hbPrefix.h"
#include "hbLog.h"

#include "d2Conf.h"
#include "xmlDefReader.h"
#include "state.h"

#ifdef WIN32
#define min(a,b) ( ( (a) < (b) ) ? (a) : (b) )
#define max(a,b) ( ( (a) < (b) ) ? (b) : (a) )
#endif

long _getAttrDkObjId( ddpConnection& ddpConn, const hbString& dkName )
{
	try
	{
		ddpQuery Q( ddpConn, dkName );

		ddpList Lst;
		Q.read( Lst );

		if( !Lst.size() )
			return -1;

		ddpObject obj = Lst.front();
		hbString idCol = obj.datakind().primaryKey().column( 0 ).name();

		return obj[ idCol ];
	}
	catch( const hbException& ex )
	{
		HbErr( "! Fehler in _getAttrDkId() !" )
		HbErr( ex );

		return -1;
	}



}

long _detectLocType( ddpConnection& ddpConn, const hbString& dkName, long eaid )
{
	try
	{
		ddpQuery Q( ddpConn, "sys_global_id" );
		Q.filter( list<long>( 1, eaid ) );

		ddpList Lst;
		Q.read( Lst );

		if( Lst.size() )
		{
			long locTypeId = Lst.front()["type"];

			if( !ddpConn.hasDatakind( locTypeId ) )
			{
				HbErr( "Fuer Datenart '" << dkName << "' und Id '" << eaid << 
					   "' kann keine Objekt-Typ-Id bestimmt werden." )

				return -1;
			}
			else
			{
				cLog << "Fuer Datenart '" << dkName << "' (mit Id '" << eaid << "') "
				        "Objekt-Typ-Id '" << locTypeId << "' bestimmt." << endl;

				return locTypeId;
			}
		}
		else
		{
			HbErr( "Fuer Datenart '" << dkName << "' und Id '" << eaid << 
				   "' kann keine Objekt-Typ-Id bestimmt." )

			return -1;
		}
	}
	catch( const hbException& ex )
	{
		HbErr( "! Fehler in _detectLocType() !" )
		HbErr( ex );

		return -1;
	}
}



///////////////////////////////////////////////////////////////////////////////////////
// loc_access
///////////////////////////////////////////////////////////////////////////////////////

loc_access::loc_access( const string& access , bool add_to_cache ) 
{
	hbPrefixT pref;
	LogE2( pref << __FUNCTION__ << "()" << endl; );
	
	string item = get_word_from_wlist( access , ':' , 1 ); 
	unsigned i = 1;
	while ( item != "" )
	{
		item = item.substr( 1 , item.length() - 2 );
		string in = get_word_from_wlist( item , ',' , 1 ); 
		string dk = get_word_from_wlist( item , ',' , 2 ); 
		string out = get_word_from_wlist( item , ',' , 3 ); 
		
		LogG3( pref << "add item: " << endl <<
			   pref << "col in:   " << in << endl <<
			   pref << "datakind: " << dk << endl <<
			   pref << "col out:  " << out << endl << endl; );
		
		if( add_to_cache )
			DDP_CACHE_FACT.add_item( dk , in );
		
		accItems.push_back( access_item( in ,dk ,out ) );
		i++;
		item = get_word_from_wlist( access , ':' , i ); 
	}
	
	LogE2( pref << "Ende von " << __FUNCTION__ << "()" << endl; )
} 



///////////////////////////////////////////////////////////////////////////////////////
// handler
///////////////////////////////////////////////////////////////////////////////////////

handler* handler::_instance = 0;
ddpTransaction* handler::trans = 0;
list< itemLst > handler::lists;

ddpConnection* handler::ddpConn = 0;
string handler::m_dataDk2GdObjDkFile;
bool handler::m_coordinatesMandatory = true;



handler* handler::get_handler()
{ 
	if( !_instance )
	{
		_instance = new handler();
		trans = new ddpTransaction( *(handler::ddpConn) );
	}
	return _instance;
};




void handler::buildMST( const hbString& definition )
{
	d2MST					mst;
	d2RoadFilterLst			roadFilter;
	d2ObjNameFilterLst		objNameFilter;
	d2DkAttrDefLst			dkAttrDefLst;

	hbString errTxt;
	if( !XML_DEF_READER.parseDef( definition, mst, roadFilter, objNameFilter, dkAttrDefLst, errTxt ) )
	{
		hbString _errTxt = "Fehlerhafte MST-Definition: " + errTxt;
		throw d2MSTException( D2_MST_ERR_INVALID_MST_DEF, _errTxt );
	}

	if( !LOC_MATCH.init( roadFilter, objNameFilter ) )
	{
		hbString errTxt = "Ungueltige Location-Filter";
		throw d2MSTException( D2_MST_ERR_INVALID_LOC_FILTER, errTxt );
	}

	lists.clear();

	d2DkAttrDefLst::iterator it;

	for( it = dkAttrDefLst.begin(); it != dkAttrDefLst.end(); ++it )
	{
		d2DkAttrDef& def = *it;
	
		handle( mst, def ); 
	}
	
	writeState( mst.getName(), mst.getVersion(), savingRecords, "" );

	commit( mst );  
}


void handler::add_restr( const string& dk )
{
	try
	{
		long eatype;
		
		ddpList qlst;
		
		map< hbString, long >::iterator itm = m_dkNmae2eatId.find( dk );
		if( itm != m_dkNmae2eatId.end() )
		{
			ddpQuery q2( *ddpConn, "eat" );
			q2.filter( itm->second , "datakind" );
			q2.read( qlst );   
			
			if( qlst.size() )
			{
				ddpObject obj = *(qlst.begin());
				eatype = obj["eatypid"].asLong();
				DDP_CACHE_FACT.add_cache_restr( cache_restrict( "listdea" ,  
			 											        "eatype",
														        ValueT(eatype) )); 
			}
		}
		else
		{
			HbErr( "!!! handler::add_restr(): Unbekannte Datenart '" << dk << "' !!!" )
		}
	}
	catch( const hbException& ex )
	{
		HbErr( "DDP-Fehler" << endl << ex << endl; )

		list<string> errTexts;
		errTexts.push_back( "DDP-Fehler" );
		errTexts.push_back( ex.asString() );
		throwErrorMsg( errTexts );
	}
}

void handler::insert_dk_access( const string& dk , const string& access )
{
	vrz_loc_access[dk] = new loc_access(access);
};



void handler::init( const string& joinFile )

{
	hbPrefixT pref;
	LogE1( pref << __FUNCTION__ << "()" << endl; );

	try
	{
		m_min_x = -1;
		m_min_y = -1;
		m_max_x = -1;
		m_max_y = -1;

		if( ddpConn->hasDatakind("extreme_coor") )
		{
			// extreme_coor

			ddpQuery q( *ddpConn , "extreme_coor" );         
			ddpList dkLst;
			q.read( dkLst );

			if( dkLst.size() > 0 )
			{
				ddpObject& obj = dkLst.front();

				m_min_x = obj["min_x"];
				m_min_y = obj["min_y"];
				m_max_x = obj["max_x"];
				m_max_y = obj["max_y"];
			}
		}
		


		// Zuordnung Datenartname --> eat::eatypid bestimmen

		map< long, long > derived2Base;

		ddpQuery q( *ddpConn , "datakind" );         
		ddpList dkLst;
		q.read( dkLst );

		ddpList::iterator it;
		for( it = dkLst.begin(); it != dkLst.end(); ++it )
		{
			ddpObject& obj = *it;

			if( !obj["DestData"].isNull() )
				derived2Base[ obj["DestData"].asLong() ] = obj["DataId"];

			m_dkNmae2eatId[ obj["DkName"].asString() ] = obj["DataId"];
		}


		// Für abgeleitete Datenarten ist die dataID der Basis-Datenart zu bestimmen

		map< hbString, long >::iterator itm;
		for( itm = m_dkNmae2eatId.begin(); itm != m_dkNmae2eatId.end(); ++itm )
		{
			long id = itm->second;

			map< long, long >::iterator itl = derived2Base.find( id );
			while( itl != derived2Base.end() )
			{
				id = itl->second;
				itl = derived2Base.find( id );
			}

			LogD3( "Datenart: " << itm->first << ", eatypid: " << id << endl )
			itm->second = id;
		}
	}
	catch( const hbException& ex )
	{
		list<string> errTexts;
		errTexts.push_back( "DDP-Fehler beim Lesen von 'datakind':");
		errTexts.push_back( ex.asString() );

		throwErrorMsg( errTexts );
	}

	
	// parse location lookup specification
	
	cLog << "Cache-Initialisierung: stand by !" << endl;
	
	char line[1000];
	
	
	ifstream inp;
	inp.open( joinFile.c_str() , ios::in);
	
	if (!inp.is_open())
	{
		hbString errStr = "Datei '" + joinFile + "' kann nicht geoeffnet werden";
		HbErr( errStr << endl; )

		list<string> errTexts;
		errTexts.push_back( errStr );
		throwErrorMsg( errTexts );
	}
	
	while(!inp.eof()) 
	{
		inp.getline( line , 1000 );   
		string dk = line;
		
		if ( trim( line ) != "" )
		{
			if( trim( line )[0] != '#' )
			{
				LogH1( "datakind: " << dk << endl; );  
				
				if( inp.eof() )
				{
					hbString errStr = "Fehler in Datei der ZDF-Datenart-Zugriffsbeschreibung: '" + 
						              joinFile + "'";
					HbErr( errStr << endl; )

					list<string> errTexts;
					errTexts.push_back( errStr );
					throwErrorMsg( errTexts );
				}
				
				inp.getline( line , 1000 );      
				string access = line;
				
				LogH1( "access:   " << access << endl; ); 
				
				insert_dk_access( dk , access );
			}
		}
	}
	
	string cycle_join = string("(qid,q,listglane):") +
		"(listglid,listglane,glaneid):" +
		"(glaneid,glane,listlane):" +
		"(listlid,listlane,laneid):" +
		"(laneid,lanedesc,listdea):" +
		"(listeaid,listdea,eaid):" +
		"(eaid,DaLVEBetriebsParamIst,IVLaengeKurz)";

	try
	{
		cycle_acc = loc_access( cycle_join );
	}
	catch( const psErrorT& ex )
	{
		hbString errStr = "Fehler beim Zugriff auf ZDF-Datenmodell: '" + 
						  cycle_join + "' (" + ex.asString() + ")";
		HbErr( errStr << endl; )

		list<string> errTexts;
		errTexts.push_back( errStr );
		throwErrorMsg( errTexts );
	}	
	
	LogE1( pref << "Ende von " << __FUNCTION__ << "()" << endl; );
}


bool handler::check_column( const string& datakind , const string& col ) 
{ 
	try
	{ 
		ddpDatakind dk( *ddpConn , datakind );
		bool ret = dk.hasColumn( col );
		return ret;
	}
	catch( const hbException& ex )
	{
		cerr << "!!! unknown datakind '" << datakind << "' !!!" << endl;
		cerr << ex << endl;
		return false;
	}
}


///////////////////////////////////////////////////////////////////////////////////////////////
// Konvertierung
///////////////////////////////////////////////////////////////////////////////////////////////

string handler::get_data_object( const string& unit )
{
	if( unit == CONF_UNIT_V )
		return MPT_DO_V;
	else if( unit == CONF_UNIT_Q )
		return MPT_DO_Q;
	else if( unit == CONF_UNIT_Q_I )
		return MPT_DO_Q;
	else if( unit == CONF_UNIT_Q_5 )
		return MPT_DO_Q;
	else if( unit == CONF_UNIT_D )
		return MPT_DO_D;
	else
	{
		hbString errTxt = "get_data_object(): Unbekannte Unit '" + unit + "'";
		throw d2MSTException( D2_MST_ERR_UNKNOWN_UNIT_CODE, errTxt );
	}
}


string handler::get_mpt_unit( const string& unit )
{
	if( unit == CONF_UNIT_V )
		return MPT_UNIT_V;
	else if( unit == CONF_UNIT_Q )
		return MPT_UNIT_Q;
	else if( unit == CONF_UNIT_Q_I )
		return MPT_UNIT_Q_I;
	else if( unit == CONF_UNIT_Q_5 )
		return MPT_UNIT_Q_5;
	else if( unit == CONF_UNIT_D )
		return MPT_UNIT_D;
	else
	{
		hbString errTxt = "get_mpt_unit(): Unbekannte Unit '" + unit + "'";
		throw d2MSTException( D2_MST_ERR_UNKNOWN_UNIT_CODE, errTxt );
	}
}

string handler::get_veh_class( const string& vehicle_kind )
{
	if( vehicle_kind == CONFVEH_CLASS_P )
		return MPT_VEH_CLASS_P;
	else if( vehicle_kind == CONFVEH_CLASS_L )
		return MPT_VEH_CLASS_L;
	else if( vehicle_kind == CONFVEH_CLASS_K )
		return MPT_VEH_CLASS_K;
	else
	{
		hbString errTxt = "get_veh_class(): Unbekannte Fahrzeugart '" + vehicle_kind + "'";
		throw d2MSTException( D2_MST_ERR_UNKNOWN_VEH_CLASS, errTxt );
	}
};



#define _trafficFlow					11
#define _trafficSpeed					13
#define _traffictrafficConcentration	10
#define _unknown						15				


long _dataObj2DataKind( const hbString& dataObj )
{
	if( dataObj == MPT_DO_V )
		return _trafficSpeed;
	else if( dataObj == MPT_DO_Q )
		return _trafficFlow;
	else if( dataObj == MPT_DO_D )
		return _traffictrafficConcentration;
	else
	{
		HbErr( "Unbekanntes data object '" << dataObj << "'" << endl )
		return _unknown;
	}
}


#define _anyVehicle			0
#define _car				3
#define _lorry				13	
#define _unknownVehClass	23

long _fzKlasse2vehType( const hbString& fzKlasse )
{
	if( fzKlasse == MPT_VEH_CLASS_P )
		return _car;
	else if( fzKlasse == MPT_VEH_CLASS_L )
		return _lorry;
	else if( fzKlasse == MPT_VEH_CLASS_K )
		return _anyVehicle;
	else
	{
		HbErr( "Unbekannte Fahrzeugklasse '" << fzKlasse << "'" << endl )
		return _unknownVehClass;
	}
}

#define __anyVehicle		0
#define __lorry				1	
#define __car				2
#define __unknownVehClass	3


long _inpFzKlasse2Enum( const hbString& fzKlasse )
{
	if( fzKlasse == CONFVEH_CLASS_P )
		return __car;
	else if( fzKlasse == CONFVEH_CLASS_L )
		return __lorry;
	else if( fzKlasse == CONFVEH_CLASS_K )
		return __anyVehicle;
	else
	{
		HbErr( "Unbekannte Fahrzeugklasse '" << fzKlasse << "'" << endl )
		return __unknownVehClass;
	}
}


#define INP_UNIT_UNKNOWN_NUM	0
#define INP_UNIT_V_NUM			1
#define INP_UNIT_Q_NUM			2
#define INP_UNIT_Q_I_NUM		3
#define INP_UNIT_Q_5_NUM		4
#define INP_UNIT_D				5

long _inpUnit2Enum( const hbString& unit )
{
	if( unit == CONF_UNIT_V )
		return INP_UNIT_V_NUM;
	else if( unit == CONF_UNIT_Q )
		return INP_UNIT_Q_NUM;
	else if( unit == CONF_UNIT_Q_I )
		return INP_UNIT_Q_I_NUM;
	else if( unit == CONF_UNIT_Q_5 )
		return INP_UNIT_Q_5_NUM;
	else if( unit == CONF_UNIT_D )
		return INP_UNIT_D;
	else
	{
		hbString errTxt = "get_mpt_unit(): Unbekannte Unit '" + unit + "'";
		throw d2MSTException( INP_UNIT_UNKNOWN_NUM, errTxt );
	}
}



bool handler::check( const d2MST& mst, const d2DkAttrDef& dkAttrDef, hbString& errTxt )
{
	string mst_name				= mst.getName();

	string datakind				= dkAttrDef.getDatakindName();	
	string id_name				= dkAttrDef.getDatakindIdCol();
	string value_name			= dkAttrDef.getDatakindValueCol();	
	string valid_name			= dkAttrDef.getDatakindStateCol();	

	LogH1(	endl << "Prüfe:" << endl <<
			"MST-Kennung              " << mst_name << endl <<
			"Datenartname             " << datakind << endl <<
			"Name der ID-Spalte       " << id_name << endl <<
			"Name der Werte-Spalte    " << value_name << endl <<
			"Name der Guelt.-Spalte   " << valid_name << endl <<
			endl;
		 );
	
	
	if( trim( valid_name ) != "" )
	{
		if( !check_column( datakind , valid_name ) )
		{
			errTxt = "Unbekannte Datenart oder Spalte: '" + datakind + ":" + valid_name + "'";
			return false;
		}
	}
	
	if( !check_column( datakind , id_name ) ||
		!check_column( datakind , value_name )
		)
	{
		errTxt = "Unbekannte Spalte: '" + value_name + "' oder '" + id_name + "'";
		return false;
	}

	return true;
}

void handler::handle( const d2MST& mst, const d2DkAttrDef& dkAttrDef )
{
	string mst_name				= mst.getName();

	long valid_interval			= dkAttrDef.getValidInterval();
	string unit					= dkAttrDef.getValueUnit();
	string datakind				= dkAttrDef.getDatakindName();	
	string id_name				= dkAttrDef.getDatakindIdCol();
	string value_name			= dkAttrDef.getDatakindValueCol();	
	string valid_name			= dkAttrDef.getDatakindStateCol();	
	string vehicle_kind			= dkAttrDef.getVehicleClass();

	string invalid_vals;
	
	hbStringLst invVals = dkAttrDef.getDatakindInvalidVals();

	hbStringLst::iterator iti;
	for( iti = invVals.begin(); iti != invVals.end(); ++iti )
	{
		invalid_vals += *iti + ",";
	}

	// endständiges Komma entfernen
	if( invalid_vals.size() )
		invalid_vals = invalid_vals.substr( 0, invalid_vals.size() - 1 );


	try
	{
		LogH1(	endl << "Verarbeite:" << endl <<
				"MST-Kennung              " << mst_name << endl <<
				"Zeitschranke             " << valid_interval << endl <<
				"Einheit des Messwertes   " << unit << endl <<
				"Datenartname             " << datakind << endl <<
				"Name der ID-Spalte       " << id_name << endl <<
				"Name der Werte-Spalte    " << value_name << endl <<
				"Name der Guelt.-Spalte   " << valid_name << endl <<
				"Ungueltig-Werte          " << invalid_vals << endl <<  
				"Fahrzeugklasse           " << vehicle_kind << endl << 
				endl;
		     );
		
		
		if( trim( valid_name ) != "" )
		{
			if( !check_column( datakind , valid_name ) )
			{
				hbString errStr = "Unbekannte Datenart oder Spalte: '" + 
								  datakind + ":" + valid_name + "'";
				HbErr( errStr << endl; )

				list<string> errTexts;
				errTexts.push_back( errStr );
				throwErrorMsg( errTexts );
			}
		}
		
		if( !check_column( datakind , id_name ) ||
			!check_column( datakind , value_name )
			)
		{
			hbString errStr = "Unbekannte Spalte: '" + 
							  value_name + "' oder '" + id_name + "'";
			HbErr( errStr << endl; )

			list<string> errTexts;
			errTexts.push_back( errStr );
			throwErrorMsg( errTexts );
		}

		writeState( mst.getName(), mst.getVersion(), configurationParsed, "" );

		ddpConnection DDPConn = *ddpConn; 
		
		locConnection locConn( DDPConn );
		itemLst lst;  
		
		// Data object , Fahrzeugklasse, Einheit des Meßwertes
		
		long period = -1;
		string lanes = "";
		string equip;
		
		list<long> IDs; 
		map<long,long> geoIDs;
		
		
		// IDs bestimmen
		
		if( dk_is_gd( datakind ) )
		{
			// Attribut zu Geodyn-Datenart
			
			ddpDatakind attr_dk( DDPConn ,datakind );
			ddpReferenceSet refs = attr_dk.referenceTo();
			
			string obj_dk_name; 
			if( !refs.size() )
			{
				if( datakind == "DaLVEAnalyseWerteOpt0" )
					obj_dk_name = "tr_detection_site";
				else
				{
					// Datenart aus zusätzlicher Versorgung "dataDk2GdObjDk.txt"bestimmen

					ifstream d2g;
					char line[1000];
					d2g.open( m_dataDk2GdObjDkFile.c_str(), ios::in );

					if (!d2g.is_open())
					{
						string errStr = "Datei '" + m_dataDk2GdObjDkFile + "' kann nicht geoeffnet werden !";
						HbErr( errStr << endl; )

						list<string> errTexts;
						errTexts.push_back( errStr );
						throwErrorMsg( errTexts );
					}

				
					while( !d2g.eof() ) 
					{
						d2g.getline( line , 1000 );   
				
						if ( trim( line ) != "" )
						{
							hbString L = line;

							vector<hbString> parts;
							L.split( " ", parts );

							if( parts.size() != 2 )
							{
								hbString errStr = "Fehlerhafte Zeile in '" + 
												  datakind + "' konnte nicht bestimmt werden";
								HbErr( errStr << endl; )

								list<string> errTexts;
								errTexts.push_back( errStr );
								throwErrorMsg( errTexts );
							}

							if( parts[0] == datakind )
							{
								obj_dk_name = parts[1];
								break;
							}
						}
					}
				}
			}
			else
			{			
				ddpDatakind obj_dk = (*(refs.begin())).master();
				obj_dk_name = obj_dk.name(); 
			}


			if( obj_dk_name.empty() )
			{
				hbString errStr = "Master-Datenart zu '" + 
								  datakind + "' kann nicht bestimmt werden";
				HbErr( errStr << endl; )

				list<string> errTexts;
				errTexts.push_back( errStr );
				throwErrorMsg( errTexts );
			}

			
			// Location IDs bestimmen
			
			long obj_dk_id = get_object_datakind_id( obj_dk_name );
			locLayer loc_layer( locConn , obj_dk_id );
			
			loc_layer.getLocations( IDs );
			
			list<long>::iterator it;
			for( it = IDs.begin() ; it != IDs.end() ; ++it )
				geoIDs[ *it ] = *it;
		}
		else
		{
			// VRZ datakind:
			// access path: 
			//
			// datakind( name=datakind ) 
			// --(dataid,datakind)--> eat
			// --(eatyp,eatypid)-->ea
			
			
			DDP_CACHE_FACT.clear_cache_restr();         
			add_restr( datakind );
			DDP_CACHE_FACT.re_init( "listdea" );
			
			ddpList qlst;
			
			map< hbString, long >::iterator itm = m_dkNmae2eatId.find( datakind );
			if( itm == m_dkNmae2eatId.end() )
			{
				HbErr( "!!! handler::add_restr(): Unbekannte Datenart '" << datakind << "' !!!" )
				exit(1);
			}

			ddpQuery q2( DDPConn , "eat" );
			q2.filter( itm->second, "datakind" );
			ddpQuery q3( DDPConn , "ea" );
			q3.filter( q2 , "eatypid" , "eatyp" );
			q3.read( qlst );   
			
			LogH1( "Anzahl ZDF IDs: " << qlst.size() << endl; )       
			ddpList::iterator it;
			for( it = qlst.begin() ; it != qlst.end() ; ++it )
				IDs.push_back( (*it)["eaid"] );
			
			// get Geodyn location IDs
			
			loc_access* ptl = HANDLER.get_loc_access( datakind );
			
			if( !ptl )
			{
				hbString errStr = "Kein Zugriffspfad fuer Datenart '" + datakind + "'";
				HbErr( errStr << endl; )

				list<string> errTexts;
				errTexts.push_back( errStr );
				throwErrorMsg( errTexts );
			}
			
			DDP_CACHE_FACT.getValue( IDs , *ptl , geoIDs );         
			
			LogH1( "Umsetzung ZDF --> DB: " << endl <<
				   "Anzahl ZDF IDs:    " << IDs.size() << endl <<
				   "Anzahl DB IDs: " << geoIDs.size() << endl )
			
			typedef map<long,long> mll;
			LogCodeH3( cLog << "geoIDs:" << endl;
					   mll::iterator itm;   
					   for( itm = geoIDs.begin() ; itm != geoIDs.end() ; ++itm )
							cLog << itm->first << ":" << itm->second << endl;
				     );
		}
		
		// loop over IDs
		
		list<long>::iterator it;
		
		for( it = IDs.begin() ; it != IDs.end() ; ++it )
		{
			long ea_id = *it;
			period = -1;			
			
			LogH1( "---------- Bearbeite ID "<< ea_id << " ----------" << endl; )
			
			try
			{
				//  ??? betroffene Fahrtrichtung ???
				
				string loc_name;
				long cx = -1, cy = -1;
				string rds_loc_tbl_ref = ""; 
				string rds_db_ver = ""; 
				long rds_loc_code = 0;
				char rds_direction = '\0';
				long rds_pt_distance = 0;
				
				if( geoIDs.find( *it ) != geoIDs.end() )
				{
					
					long id = geoIDs[ *it ];

					locLocation loc( locConn , id );
					loc_name = loc.name();
					LogH1( "Name: " << loc_name << endl; );

					if( !LOC_MATCH.match( id, loc.permanentId() ) )
					{
						LogH1( "Location-Filter nicht erfuellt: MP " << "nicht aufgenommen." << endl; )
						continue;
					}


					// RDS-Location-Angaben bestimmen
					
					locRDSInfo rds_info;
					
					
					if( loc.getRDSInfo( rds_info ) )
					{
						rds_loc_tbl_ref = rds_info.dbid(); 
						unsigned pos = rds_loc_tbl_ref.find( ":" );
						if( pos != string::npos )
						{
							// ### should always work

							rds_db_ver = rds_loc_tbl_ref.substr( pos + 1 );
							rds_loc_tbl_ref = rds_loc_tbl_ref.substr( 0, pos );
						}

						rds_loc_code = rds_info.primaryLoc();
						
						locLocation::Direction dir = rds_info.direction();
						switch( dir )
						{
							case locLocation::None:   
								rds_direction = 'U';
								break;  
							case locLocation::Both:   
								rds_direction = 'B';
								break;  
							case locLocation::Negativ:   
								rds_direction = 'N';
								break;  
							case locLocation::Positiv:   
								rds_direction = 'P';
								break;  
						}
						
						rds_pt_distance = long(rds_info.primaryOffset());
					}
					

					if( !m_coordinatesMandatory )
					{
						cx = 1.0;
						cy = 1.0;
					}
					
					// Koordinaten bestimmen 
					
					try
					{
						locGeo coor;
						loc.getCoordinates(coor);
						list<locUnit> ulst = coor.unit();
						if ( ulst.size() )
						{
							locUnit u = *(ulst.begin()); 
							list<locPoint> plst = u.coor(); 	
							if( plst.size() )
							{
								locPoint pt = *(plst.begin());
								cx = pt.x();
								cy = pt.y();

								if( m_min_x != -1 )
								{
									if( 
										( cx < m_min_x ) ||
										( cx > m_max_x ) ||
										( cy < m_min_y ) ||
										( cy > m_max_y ) 
									  )
									{
										cx = -1;
										cy = -1;
									}
								}
							}
						}
					}
					catch( const hbException& err )
					{
						cerr << "id: " << ea_id << endl;
						cerr << err << endl;
					}	
					
				}
				else
				{
					LogH1( "EQ kann nicht bestimmt werden" << endl; );
				}    
				
				// Erfassungshardware, Erfassungszyklus
				
				
				if( cx >= 0 )
				{
					
					// build data item
					
					item obj;
					
					if( dk_is_gd( datakind ) ) 
					{
						lanes = "A";
						
						long qtype;   
						loc_access access( "(qid,q,qtype)" ,
							false );
						if( DDP_CACHE_FACT.getValue( ea_id , access , 
							qtype ) != psObj::OK )	
						{
							equip = "unbekannt";
							cerr << "!!! qtype not available for EQ "
								 << "(" << loc_name << "," << ea_id << ") !!!"
								 << endl; 
						}        
						
						else if( qtype == QTYPE_TLS )
						{
							equip = EQUIPMENT_TLS;
							
							// get period form DaLVEBetriebsParamIst
							
							if( DDP_CACHE_FACT.getValue( ea_id , cycle_acc , 
								period ) != psObj::OK )	
							{
								period = -1;
								cerr << "!!! period not available for EQ "
									 << "(" << loc_name << "," << ea_id << ") !!!"
									 << endl; 
							}        
							else
								period = period / 4;
						}
						else if( qtype == QTYPE_VKE )
						{
							period = EQUIP_PERIOD;
							equip = EQUIPMENT_VKE;
						}
					}
					else
					{
						long qtype;   
						loc_access* ptl = HANDLER.get_loc_access( datakind );
						
						if( !ptl )
						{
							hbString errStr = "Kein Zugriffspfad fuer Datenart '" + datakind + "'";
							HbErr( errStr << endl; )

							list<string> errTexts;
							errTexts.push_back( errStr );
							throwErrorMsg( errTexts );
						}
						
						long ll;
						if( DDP_CACHE_FACT.getValue( ea_id , *ptl , 
							"lanedesc" , "lanelage" , 
							ll ) == psObj::OK )	
						{
							if( !ll )
								lanes = "H";
							else
								lanes = string("") + ll;
						}
						
						if( DDP_CACHE_FACT.getValue( ea_id , *ptl , 
							"q" , "qtype" , 
							qtype ) != psObj::OK )	
						{
							equip = "unbekannt";
							cerr << "!!! qtype not available for lnae "
								 << "(" << loc_name << "," << ea_id << ") !!!"
								 << endl; 
						}        
						
						else if( qtype == QTYPE_TLS )
						{
							if( datakind == "DaLVE16Bit_FehlerOpt0" ) 
							{
								// !!!!!!!!!!
								// ignore faulty VKE ids: 
								// !!!!!!!!!!
								//
								// database supply error
								continue;
							}
							
							equip = EQUIPMENT_TLS;
							
							// get period form DaLVEBetriebsParamIst
							
							loc_access acc("(eaid,DaLVEBetriebsParamIst,IVLaengeKurz)",
								false);
							if( DDP_CACHE_FACT.getValue( ea_id , acc , 
								period ) != psObj::OK )	
							{
								period = -1;
								cerr << "!!! period not available for lane "
									 << "(" << loc_name << "," << ea_id << ") !!!"
									 << endl; 
							}        
							else
								period = period / 4;
						}
						else if( qtype == QTYPE_VKE )
						{
							period = EQUIP_PERIOD;
							equip = EQUIPMENT_VKE;
						}
					}
					
					if( lanes == "" )
					{
						LogH1( "Betroffene Spuren können nicht " << "bestimmt werden: " <<
							   "MP nicht aufgenommen." << endl; );   

						continue;
					} 
					
					if( period > 0 )
					{ 
						obj["mpt"] = mst_name;
						obj["mp_ref"] = ea_id + string("_") + 
							get_data_object( unit ) + "_" +
							vehicle_kind.substr( 0 , 1 );                
						obj["ms_data_object"] = get_data_object( unit );
						obj["rds_loc_tbl_ref"] = rds_loc_tbl_ref;
						obj["rds_loc_tbl_ver"] = rds_db_ver;
						obj["rds_loc_code"] = (int)rds_loc_code;
						obj["rds_direction"] = rds_direction;
						obj["rds_pt_distance"] = (int)rds_pt_distance;
						obj["ms_name"] = loc_name;
						//obj["ms_direction"] = rds_direction;
						
						obj["ms_lanes"] = lanes;
						
						obj["ms_period"] = (int)period;

						obj["ms_veh_class"] = get_veh_class( vehicle_kind );
						obj["ms_inp_veh_class"] = vehicle_kind;

						obj["ms_unit"] = get_mpt_unit( unit );
						obj["ms_inp_unit"] = unit;

						obj["ms_equipment"] = equip;
						obj["data_kind"] = datakind;
						obj["id_name"] = id_name;
						obj["vrz_id"] = (int)ea_id;
						obj["value_name"] = value_name;
						obj["valid_col"] = valid_name;
						obj["invalid_vals"] = invalid_vals;
						obj["valid_interval"] = (int)valid_interval;
						obj["coord_x"] = (int)cx;
						obj["coord_y"] = (int)cy;   
						
						LogH2( obj << endl; );
						
						lst.push_back( obj );
					}
					else
					{
						LogH1( "Erfassungszyklus kann nicht " <<
							   "bestimmt werden: " <<
							   "MP nicht aufgenommen." << endl; )   
						
					}
				}
				else
				{
					LogH1( "Keine gueltigen geographischen Koordinaten" << 
						   ": MP nicht aufgenommen" << endl; );
				}
			}
			catch( const hbException& err )
			{
				cerr << "id: " << ea_id << endl;
				cerr << err << endl;
			}	
		}
	   
		LogH1( lst.size() <<" MeasurementPoints hinzugefuegt." << endl; );  

		lists.push_back( lst );
	}
	catch( const hbException& ex )
	{
		HbErr( "DDP-Fehler" << endl << ex << endl; )

		list<string> errTexts;
		errTexts.push_back( "DDP-Fehler" );
		errTexts.push_back( ex.asString() );

		throwErrorMsg( errTexts );
	}	
};






void handler::createRecords( const d2MST& mst,
							 ddpObject& mstObj,
							 ddpList& mstDefRecords,
							 ddpList& items,
							 ddpList& dataSources,
							 ddpList& locations,
							 ddpList& filterObjects,
							 set<int>& sIds
							 )
{
	time_t now = hb_time(0);

	// d2MST

	mstObj["time"]				= now;
	mstObj["mst"]				= mst.getName();
	mstObj["version"]			= mst.getVersion();			
	mstObj["description"]		= mst.getDescr();	
	
	const d2ObjNameFilterLst& objFilterLst = LOC_MATCH.getObjNameFilterLst();
	d2ObjNameFilterLst::const_iterator itf;
	for( itf = objFilterLst.begin(); itf != objFilterLst.end(); ++itf ) 
	{
		const d2ObjNameFilter& objNameFilter = *itf;		

		hbStringLst laneIds = objNameFilter.getLaneIds();
		if( laneIds.size() == 0 )
		{
			ddpObject obj( *ddpConn, "d2MSTFilterObject" );
			obj["time"]			= now;
			obj["mst"]			= mst.getName();
			obj["locId"]		= objNameFilter.getName();
			obj["laneId"]		= hbString("-1");

			filterObjects.push_back( obj );
		}
		else
		{
			hbStringLst::const_iterator itl;
			for( itl = laneIds.begin(); itl != laneIds.end(); ++itl )
			{
				ddpObject obj( *ddpConn, "d2MSTFilterObject" );
				obj["time"]			= now;
				obj["mst"]			= mst.getName();
				obj["locId"]		= objNameFilter.getName();
				obj["laneId"]		= *itl;

				filterObjects.push_back( obj );
			}
		}
	}


	// d2MSTDef

	hbString mst_def = mst.getDef();

	unsigned partLen = ddpConn->datakind( "d2MSTDef" ).column( "defintion" ).count();

	unsigned count = 0;
	if( mst_def.size() )
		count = ( mst_def.size() - 1 ) / partLen + 1;

	unsigned i;
	for( i = 0; i < count; i++ )
	{
		ddpObject obj( *ddpConn, "d2MSTDef" );
		obj["time"]			= now;
		obj["mst"]			= mst.getName();
		obj["version"]		= mst.getVersion();
		obj["count"]		= i;

		unsigned beg = i * partLen;
		unsigned len = min( mst_def.size() - beg, partLen );

		obj["defintion"]	= mst_def.substr( beg, len );

		mstDefRecords.push_back( obj );
	}


	int maxId = -1;
	{
		// Read records of datakind 'd2MSTDataSource'
		// Use id values for new records, erase surplus ones.

		ddpQuery Q( *ddpConn, "d2MSTDataSource" );
		Q.condition( "mst = '" + mst.getName() + "'" );
		ddpList Lst;
		Q.read( Lst );

		ddpList::iterator it;
		for( it = Lst.begin(); it != Lst.end(); ++it )
		{
			int id = (*it)["id"].asInt();
			sIds.insert( id );
			maxId = max( maxId, id );
		}
	}


	// d2MSTItem

	list< itemLst >::iterator it;
	for( it = lists.begin(); it != lists.end(); ++it )
	{
		itemLst& ILst = *it;

		itemLst::iterator iti;
		for( iti = ILst.begin(); iti != ILst.end(); ++iti )
		{
			item& I = *iti;

			if( iti == ILst.begin() )	
			{
				// d2MSTDataSource

				ddpObject obj( *ddpConn, "d2MSTDataSource" );

				if( sIds.size() )
				{
					dataSourceCount = *(sIds.begin());
					sIds.erase( sIds.begin() );
				}
				else
				{
					maxId++;
					dataSourceCount = maxId;
				}


				obj["mst"]	= mst.getName();
				obj["id"]	= dataSourceCount;
				obj["time"]	= now;

   				obj["dataType"]			= _dataObj2DataKind( I["ms_data_object"].asString() );

   				obj["vehClass"]			= _fzKlasse2vehType( I["ms_veh_class"].asString() );
				obj["unit"]				= I["ms_unit"].asString();

   				obj["inpVehClass"]		= _inpFzKlasse2Enum( I["ms_inp_veh_class"].asString() );
				obj["inpUnit"]			= _inpUnit2Enum( I["ms_inp_unit"].asString() );

   				obj["dataKind"]			= I["data_kind"].asString();
				obj["idCol"]			= I["id_name"].asString();
   				obj["valueCol"]			= I["value_name"].asString(); 
   				obj["validCol"]			= I["valid_col"].asString(); 
   				obj["invalidVals"]		= I["invalid_vals"].asString(); 
   				obj["validInterval"]	= I["valid_interval"].asLong(); 

				dataSources.push_back( obj );
			}

			ddpObject obj( *ddpConn, "d2MSTItem" );
			obj["time"]			= now;
			obj["mst"]			= mst.getName();
			obj["id"]			= itemCount;

			obj["msName"]		= I["ms_name"].asString();
			obj["msLanes"]		= I["ms_lanes"].asString();
			obj["msPeriod"]		= I["ms_period"].asLong();
			obj["msDirection"]	= I["ms_direction"];

			locKey lKey( I );


			if( locId2LocId.find( lKey ) == locId2LocId.end() )
			{
				locCount++;

				locId2LocId[lKey] = locCount;
				locId2Index[lKey] = 0;

				// Location-Objekt anlegen 

				ddpObject locObj( *ddpConn, "d2MSTLocation" );
				locObj["mst"]				= mst.getName();

				locObj["id"]				= locCount;
				locObj["time"]				= now;

				locObj["locName"]			= I["ms_name"];					// @@@ später: Unterscheidung zwischen Loc-Name und Item-Name
				locObj["equipment"]			= I["ms_equipment"];

   				locObj["rdsLocTblRef"]		= I["rds_loc_tbl_ref"];
   				locObj["rdsLocTblVer"]		= I["rds_loc_tbl_ver"];

   				locObj["rdsPrimLocCode"]	= I["rds_loc_code"];
   				locObj["rdsSecLocCode"]		= 0;

   				locObj["rdsDirection"]		= I["rds_direction"].asChar();
				locObj["rdsPrimLocDist"]	= I["rds_pt_distance"];
				locObj["rdsSecLocDist"]		= 0;

   				locObj["startCoordX"]		= I["coord_x"].asDouble();
   				locObj["startCoordY"]		= I["coord_y"].asDouble();

   				locObj["endCoordX"] = -1;
   				locObj["endCoordY"] = -1;

				locations.push_back( locObj );
			}

			locId2Index[lKey]++;
			obj["index"]		= locId2Index[lKey];

			obj["dkRef"]	=  	dataSourceCount;
			obj["dkId"]		=	I["vrz_id"].asLong();
			obj["locId"]	=	locId2LocId[lKey];

			items.push_back( obj );

			itemCount++;
		}
	}

	if( ( items.size() == 0 ) || ( locations.size() == 0 ) )
	{
		list<string> errTexts;
		errTexts.push_back( "MST-Defintion liefert leeere MST !" );

		throwErrorMsg( errTexts );
	}
}

void handler::commit( const d2MST& mst )
{
	try
	{
		locId2LocId.clear();
		locId2Index.clear();

		ddpObject mstObj( *ddpConn, "d2MST" );
		ddpList mstDefRecords;
		ddpList items;
		ddpList dataSources; 
		ddpList locations;
		ddpList filterObjects;

		itemCount		= 1;
		locCount		= 0;
		dataSourceCount = 0;	


	    set<int> sIds;
		createRecords( mst, mstObj, mstDefRecords, items, dataSources, locations, filterObjects, sIds );


		hbString delSrcCond = "( mst = '" + mst.getName() + "' ) and ( id in (";
		set<int>::iterator its;
		for( its = sIds.begin(); its != sIds.end(); ++its )
		{
			if( its != sIds.begin() )
				delSrcCond += ",";

			delSrcCond += hbString() + *its;
		}
		delSrcCond = ") )";


		trans->erase( "d2MSTFilterObject",	"mst = '" + mst.getName() + "'" );
		trans->erase( "d2MSTLocation",		"mst = '" + mst.getName() + "'" );

		if( sIds.size() )
			trans->erase( "d2MSTDataSource", delSrcCond );

		trans->erase( "d2MSTItem",			"mst = '" + mst.getName() + "'" );
		trans->erase( "d2MSTDef",			"mst = '" + mst.getName() + "'" );
		//trans->erase( "d2MST",				"mst = '" + mst.getName() + "'" );
		
		ddpList mstLst;
		mstLst.push_back( mstObj );
		trans->put( mstLst );

		trans->put( mstDefRecords );
		trans->put( dataSources );
		trans->put( locations );
		trans->put( items );
		trans->put( filterObjects );
		
		trans->commit();
	}
	catch( const hbException& ex )
	{
		list<string> errTexts;
		errTexts.push_back( "DDP-Fehler" );
		errTexts.push_back( ex.asString() );

		throwErrorMsg( errTexts );
	}	
};


long handler::get_object_datakind_id( const string& attr_dk )
{
	long eaid = _getAttrDkObjId( *ddpConn, attr_dk );

	if( eaid == -1 )
		return -1;

	return _detectLocType( *ddpConn, attr_dk, eaid );
};
	

	
//////////////////////////////////////////////////////////////////////////////////////////////////
// throwErrorMsg
//////////////////////////////////////////////////////////////////////////////////////////////////

void throwErrorMsg( const list<string>& errTexts )
{
	hbString errTxt;

	list<string>::const_iterator itt;
	for( itt = errTexts.begin(); itt != errTexts.end(); ++itt )
	{
		if( itt != errTexts.begin() )
			errTxt += "\n";

		errTxt += *itt;
	}

	throw d2MSTException( D2_MST_ERR_CREATE_DB_MST, errTxt );
}

