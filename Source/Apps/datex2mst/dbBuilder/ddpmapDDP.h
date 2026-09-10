#include "ddpmap.h"
#include "ddp.h"
#include "hbError.h"
//#include "DBInit.h"

class psObjDDP : public psObj, public ddpObject

{
public:
   psObjDDP( const ddpObject& obj ) : ddpObject( obj ) {}

   virtual ValueT  operator[] (const string& col_name) const
   {
      ValueT val;
      try
	  {
         val = ValueT ( ddpObject::operator[]( col_name ) , 
                        datakind().column( col_name ).type() ); 
      }
      catch( const hbException& ex )
	  {
         cerr << ex << endl;
         return ValueT::null;
	  }
      
      return val;
   }

   virtual psObj* clone() const { return new psObjDDP( *this ); };
};


class psDatakindDDP : public psDatakind, ddpQuery

{
public:
   psDatakindDDP( const string& name , bool sl = false ) : 
            psDatakind( name , sl ) , 
            ddpQuery( *(handler::ddpConn) , name ) {};

   void logon()
	 
   {
      subscribe();
   }

   virtual void callback( ddpQueryJob::CallbackType type )
   {
	  ddpObject obj;
	  while( getNextObject( obj ) )
         update( psObjDDP( obj ) );
   };

protected:
   virtual bool check_name() 
   { 
      try
	  { 
         ddpDatakind dk( *(handler::ddpConn) , name );
	  }
      catch( const hbException& ex )
	  {
         cerr << "!!! unknown datakind '" << name << "'" << endl;
         cerr << ex << endl;
         return false;
	  }

      return true; 
   }

   virtual bool check_columns( const list<string>& cols ) 
   { 
      try
	  { 
         ddpDatakind dk( *(handler::ddpConn) , name );

         bool hasCols = true;
         list<string>::const_iterator it;
         for( it = cols.begin() ; it != cols.end() ; ++it )
            hasCols = hasCols && dk.hasColumn( *it );
         return hasCols;
	  }
      catch( const hbException& ex )
	  {
         cerr << "!!! unknown datakind '" << name << "' !!!" << endl;
         cerr << ex << endl;
         return false;
	  }
   }

   virtual bool read_objs( list<psObj*>& lst ,
                           const list<cache_restrict>& list_restr )
   {
  	  hbPrefixT pref;

      try
	  {
         ddpQuery q( *(handler::ddpConn) , name );
         ddpList ddp_lst; 
         q.read( ddp_lst );

	     DebugH2( cout << pref << "number of items: " 
                       << ddp_lst.size() << endl; );
		 ddpList::iterator it;
         for( it = ddp_lst.begin() ; it != ddp_lst.end() ; ++it )
		 {
            psObj* obj = new psObjDDP( *it );
            bool restr_ok = (list_restr.size() == 0);

            list<cache_restrict>::const_iterator itr;
            for( itr = list_restr.begin() ; itr != list_restr.end() ; ++itr )
                restr_ok = restr_ok || (*itr).ok( obj );
            if( restr_ok )
               lst.push_back( obj );
		 }
	  }
      catch( const hbException& err )
	  {
         cerr << err << endl;
         return false;
      }	

      return true;
   }; 
};


class psDK_factDDP : public psDK_fact

{
public:

   virtual psDatakind* create_psDatakind( const string& name )
   {
      try
	  {
          psDatakind* dk = new psDatakindDDP( name ); 
          return dk;
      }
      catch( const hbException& err )
	  {
         cerr << err << endl;
         return 0;
      }	
   }

   

   static psDK_factDDP* get_psDK_factDDP()
   {
      if( !_instance )
         _instance = new psDK_factDDP();
      return _instance;
   }
private:
   psDK_factDDP() {}
   static psDK_factDDP* _instance;
};

#define DDP_CACHE_FACT (*(psDK_factDDP::get_psDK_factDDP()))
