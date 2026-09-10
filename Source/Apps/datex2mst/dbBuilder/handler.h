#ifndef _HANDLER_H_
#define _HANDLER_H_

#include <string>
#include <list>
#include <map>
#include <set>
#include "psString.h"
#include "psIterator.h"

#include "ddp.h"
#include "item.h"
#include "d2Conf.h"



#define CONF_UNIT_V		"km/h"
#define CONF_UNIT_Q		"Fzg/h"
#define CONF_UNIT_Q_I	"Fzg/I"
#define CONF_UNIT_Q_5	"Fzg/5Min"
#define CONF_UNIT_D		"Fzg/km"

#define MPT_UNIT_V		"KMH"
#define MPT_UNIT_Q		"VHH"
#define MPT_UNIT_Q_I	"VHI"
#define MPT_UNIT_Q_5	"VH5"
#define MPT_UNIT_D		"VKM"

#define MPT_DO_V "AVS"
#define MPT_DO_Q "FLO"
#define MPT_DO_D "CON"

#define CONFVEH_CLASS_P "PKW"
#define CONFVEH_CLASS_L "LKW"
#define CONFVEH_CLASS_K "KFZ"

#define MPT_VEH_CLASS_K "KX0"
#define MPT_VEH_CLASS_P "KX1"
#define MPT_VEH_CLASS_L "KX2"

#define QTYPE_TLS 10
#define QTYPE_VKE 100

#define EQUIPMENT_TLS "TLS-Erfassung"
#define EQUIPMENT_VKE "VKE-Erfassung"

#define EQUIP_PERIOD 5 

class access_item

{
public:
   access_item() {} 
   access_item( const string& in , const string& dk , const string& out )
   {
	  col_in = in;
	  datakind = dk;
	  col_out = out;
   }; 

   access_item& operator=( const access_item& acc )
   {
	  col_in = acc.col_in;
	  datakind = acc.datakind;
	  col_out = acc.col_out;

      return *this;
   }

   string col_in;
   string datakind;
   string col_out;
};

class loc_access
{
public:
   class iterator;
   friend class iterator;

   loc_access() {};
   loc_access( const string& access , bool add_to_cache = true  );
   loc_access& operator=( const loc_access& acc )
   {
      if( this == &acc )
         return *this;

      accItems = acc.accItems;       
      return *this;
   }

   class iterator : public psIterator<access_item>
   {
		
   public:
	  iterator( const loc_access* acc ) : acc_elem( acc ) { reset(); }

	  void reset() 
	  { 
		 if( acc_elem )
			it = acc_elem->accItems.begin(); 
	  }; 
		
      bool end() const
	  { 
		 if( !acc_elem )
			return true;
		 return (it == acc_elem->accItems.end()); 
	 }
   private:
	  iterator() {};
	  const loc_access* acc_elem;  
   };

private:
   list<access_item> accItems;  

};



////////////////////////////////////////////////////////////////////////////////////////////
// handler
////////////////////////////////////////////////////////////////////////////////////////////

class handler
{
public:
	typedef map< string, loc_access* > loc_access_map;

	// Erzeugt MPT-einträge für ein Attribut
	void handle( const d2MST& mst, const d2DkAttrDef& dkAttrDef );

	// Prüft Gültigkeit der Datenart- und Spalten-Namen
	bool check( const d2MST& mst, const d2DkAttrDef& dkAttrDef, hbString& errTxt );

	// ZDF-Konfiguration einlesen
	void init( const string& joinFile );

	void buildMST( const hbString& definition );

	// Speichern der MST in der Datenbank
	void commit( const d2MST& mst );

	// Umsetzen der Einzel-Datensätze in Datensätze des D2-Datenmodells
	void createRecords( const d2MST& mst,
		                ddpObject& mstObj,
						ddpList& mstDefRecords,
						ddpList& items,
						ddpList& dataSources,
						ddpList& locations,
					    ddpList& filterObjects,
					    set<int>& sIds
						);

	void currentMST( const hbString& mstName )
	{
		m_currentMST = mstName;
	}

	hbString currentMST() const
	{
		return m_currentMST;
	}

private:
	hbString m_currentMST;

private:
	loc_access_map vrz_loc_access;
	
	// Überprüft DDP-Gültigkeit des Datenart-Namens und der Spalten-Namens
	bool check_column( const string& datakind , const string& col ); 

	// true: 'dk' ist eine Location-Datenart. die Bestimmung der Attribute 
	//       erfordert keinen ZDF-Lookup der Orts-Referenz
	bool dk_is_gd( const string& dk ) 
	{ 
		return vrz_loc_access.find( dk ) == vrz_loc_access.end();
	}

	// einschränkung der ZDF-Caches auf Datenart
	void add_restr( const string& dk );

	// 'access' beschreibt den Ortsreferenz-Lookup durch das ZDF-Datenmodell 
	// für die Datenart 'dk'
	void insert_dk_access( const string& dk , const string& access );
	
	loc_access* get_loc_access( const string& dk )
	{
		if( vrz_loc_access.find( dk ) == vrz_loc_access.end() ) 
			return 0;
		return vrz_loc_access[dk];
	}  
	
	
	// Bestimmung des data object aus der Unit
	string get_data_object( const string& unit );
	
	// Umsetzung der Unit-Konstanten der MST-XML-Konfiguration in Datex-1-Units
	string get_mpt_unit( const string& unit );
	
	// Umsetzung der Fahrzeugtyp-Konstanten der MST-XML-Konfiguration in 
	// Datey-1-Konstanten
	string get_veh_class( const string& vehicle_kind );

	long get_object_datakind_id( const string& attr_dk );
	
	
	//static string lookup_loc;
	
	loc_access cycle_acc;
	
	static ddpTransaction* trans;
	static list< itemLst > lists;

private:
	// Zuordnung Datenartname (
	map< hbString, long > m_dkNmae2eatId;


public:
	static ddpConnection* ddpConn;

private:
	long itemCount;
	long locCount;
	long dataSourceCount;	

	// Location-Schlüssel (MeasurementSiteRecord-Schlüssel)
	map< locKey, long > locId2LocId;

	// Index MeasurementSpecificCharacteristics innerhalb des zugeordnteten MeasurementSiteRecords
	map< locKey, long > locId2Index;

private:
	long m_min_x;
	long m_min_y;
	long m_max_x;
	long m_max_y;


public:
	static string m_dataDk2GdObjDkFile;
	static bool m_coordinatesMandatory;


// Singleton-Implementierung ///////////////////////////////////// 

public:
	static handler* get_handler();

private:
	handler() {};
	static handler* _instance;
};

#define HANDLER (*(handler::get_handler()))



#endif
