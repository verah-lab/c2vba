#ifndef _DDPMAP_H_
#define _DDPMAP_H_

// cache for a ddp datakind
 

#include "Value.h"
#include <map>
#include <list>
#include <string>

#include "trMisc.h"
#include "handler.h"


class psObj

{
public:
   enum errCodes
   {
      UNKNOWN_COLUMN,
      NO_VALUE,
      NO_MAP,
      NO_DK_CACHE,
      OK 
   };

   virtual ~psObj() {};  
   virtual ValueT  operator[] (const string& col_name) const = 0;
   bool getValue( const string& inp , const ValueT& key ,
                  const string& outp , ValueT& val );

   virtual psObj* clone() const = 0;
};




class psObjMap

{
public:
   typedef map<ValueT,psObj*> objMap;
   typedef multimap<ValueT,psObj*> objMMap;

   psObjMap( const string& colN , list<psObj*>& objLst ) 
   {
      colname = colN;
      list<psObj*>::iterator it;
      for( it =  objLst.begin() ; it != objLst.end() ; ++it )
	  {
         objs[ (*(*it))[ colN ] ] = *it;
         objs_mult.insert( objMMap::value_type( (*(*it))[colN],*it ) );
	  }
   };

   psObj* getObj( const ValueT& key )
   {
 	  objMap::iterator it = objs.find( key );
      if( it == objs.end() )
         return 0;
      return (*it).second;
   };

   bool getObj( const ValueT& key , list<psObj*> ret_obj )
   {
      objMMap::iterator it;
      for( it =  objs_mult.lower_bound(key) ; 
           it != objs_mult.upper_bound(key) ; ++it )
         ret_obj.push_back( (*it).second );         
		   
      return true;		
   };

   bool getValue( const ValueT& key , const string& targetCol ,
                  ValueT& val )
   {
      psObj* obj = getObj( key );
      if( obj )
	  {
		 val = (*obj)[ targetCol ];
		 if ( val == ValueT::null )
		    return false;
		 return true;
	  }
      return false;
   }

   void update( const psObj& obj )
   {
 	  objMap::iterator it = objs.find( obj[colname] );
      if ( it != objs.end() )
         delete (*it).second;
      objs[ obj[ colname ] ] = obj.clone();
      //objs_mult.insert( objMMap::value_type( (*(*it))[colN],*it ) );
   }

private:
   string colname;
   objMap objs;
   objMMap objs_mult;
};


class cache_restrict

{
public: 
   cache_restrict() {}
   cache_restrict( const string& dk , const string& col ,
                   const ValueT& val ) 
   {
      this->dk = dk;
      this->col = col;
      this->val = val;
   }

   cache_restrict( const cache_restrict& restr )
   {
      dk = restr.dk;
      col = restr.col;
      val = restr.val;
   }

   bool ok( const psObj* obj ) const
   {
      return ( (*obj)[ col ] == val );
   }

   string dk;
   string col; 
   ValueT val;
};



class psDatakind

{
public:

  typedef map<string,psObjMap*> objMapLst; 

  psDatakind( const string& name , bool sl = false ) 
  {
	 hbPrefixT pref;
	 DebugG1( cout << pref << "create psDatakind: " << name 
 			       << endl; );
     this->name = name; 
     search_list = sl;
  };
  virtual ~psDatakind() {};  

  // build maps with keys
  bool init( const list<string>& cols , 
             const list<cache_restrict>& list_restr )
  {
     if( !check_name() )     
        return false;

     if( !check_columns( cols ) )
	    return false;

     if( !objList.size() )
	 {
	    if( !read_objs( objList , list_restr ) )
		   return false;
	 }

     list<string>::const_iterator it;
     for( it = cols.begin() ; it != cols.end() ; ++it )
	 {
	    hbPrefixT pref;
	    DebugG1( cout << pref << "create psObjMap: " 
                      << name << ", " << *it << endl; );

        if( objMaps.find( *it ) == objMaps.end() )
           objMaps[ *it ] = new psObjMap( *it , objList );
	 }

     return true; 
  };

  // build maps with keys
  bool re_init( const list<cache_restrict>& list_restr )
  {
     list<string> cols;
     objList.erase( objList.begin() , objList.end() );

     objMapLst::iterator it; 
     for( it = objMaps.begin() ; it != objMaps.end() ; ++it )
        cols.push_back( (*it).first );

     for( it = objMaps.begin() ; it != objMaps.end() ; ++it )
        delete (*it).second;

     objMaps.erase( objMaps.begin() , objMaps.end() );    

     return init( cols , list_restr ); 
  };


  string get_name() const { return name; }

  virtual void logon() {}

  psObj::errCodes getValue( const string& inp , const ValueT& key ,
                            const string& outp , ValueT& val )
  {
     // check column name

     list<string> cols;
     cols.push_back( inp );
     cols.push_back( outp );

     //if( !check_columns( cols ) )
     //   return psObj::UNKNOWN_COLUMN;

     objMapLst::iterator it = objMaps.find( inp );
     if( it == objMaps.end() )
     {
        if( search_list )
 		{
           list<psObj*>::iterator ito;
           for( ito = objList.begin() ; ito != objList.end() ; ++ito )
		   {
              if( (*ito)->getValue( inp , key , outp , val ) )
                 return psObj::OK;
		   }    
           return psObj::NO_VALUE;
		}
        return  psObj::NO_MAP;
	 }
     else
	 {
        psObjMap* om = (*it).second;     
        if( !om->getValue( key , outp , val ) )
		   return psObj::NO_VALUE;

        return psObj::OK;
	 }
  }

protected:
   virtual bool check_name() { return true; }
   virtual bool check_columns( const list<string>& cols ) { return true; }
   virtual bool read_objs( list<psObj*>& lst ,  
                           const list<cache_restrict>& list_restr) = 0; 

   void update( const psObj& obj )
   {
	 objMapLst::iterator it;
     for( it = objMaps.begin() ; it != objMaps.end() ; ++it )
        (*it).second->update( obj );
   }

   string name;
private:
   list<psObj*> objList;
   objMapLst objMaps;
   bool search_list;
};


class psDK_fact

{
public:
   typedef map<string,psDatakind*> dkMapLst; 
   typedef multimap<string,cache_restrict> mmap_cacheRestricts;

   typedef struct str_pair
   {
	  str_pair() {}
      str_pair( const string& str1 , const string& str2 )
	  {
         this->str1 = str1;
         this->str2 = str2;
	  } 
      string str1;
      string str2;
   };
   
   virtual ~psDK_fact() {}

   psDatakind* get_psDatakind( const string& name )
   {
      dkMapLst::iterator it = dkLst.find( name );
      if( it != dkLst.end() )
         return (*it).second;
      else
	  {
         psDatakind* dk = create_psDatakind( name );
         if ( !dk )
            return 0;
         dkLst[ name ] = dk;
         return dk; 
	  }
   }

   virtual void logon( const string& name ) 
   {
      dkMapLst::iterator it = dkLst.find( name );
      if( it != dkLst.end() )
         (*it).second->logon();
      else
	  {
         cerr << "error in psDK_fact::logon():" << endl;
         cerr << "!!! no psDatakind for '" << name <<"' !!!" << endl;
	  }      
   }

   void add_item( const string& dk , const string& col );
   void add_item( const list< str_pair >& cols );

   psObj::errCodes getValue( const string dk,
                             const string& inp , const ValueT& key ,
                             const string& outp , ValueT& val )
   {
      dkMapLst::iterator it = dkLst.find( dk );
      if( it != dkLst.end() )
	  {
         psDatakind* pDK = (*it).second;        
         return pDK->getValue( inp , key , outp , val );
	  }
      else
         return psObj::NO_DK_CACHE;
   }

   psObj::errCodes getValue( const list<long>& IDs ,
                             const loc_access& accPath ,
                             map<long,long>& values );

   psObj::errCodes getValue( long id , const loc_access& accPath ,long& lval );
   psObj::errCodes getValue( long id_in, const loc_access& accPath , 
                             const string& dk, const string col, long& lval );

   void add_cache_restr( const cache_restrict& restr )
   {
      cacheRestricts.insert( mmap_cacheRestricts::value_type( 
                                                  restr.dk , restr
                                                            ) );
   }

   void clear_cache_restr()
   {
      cacheRestricts.erase( cacheRestricts.begin() , cacheRestricts.end() );
   }

   bool re_init( const string& dk )
   {
      psDatakind* pDK = get_psDatakind( dk );
      if( pDK )
	  {
		 list<cache_restrict> list_restr;
		 get_cache_restrs( dk , list_restr );
         return pDK->re_init( list_restr );
	  }
      return false;
   }


   void get_cache_restrs( const string& dk , 
                          list<cache_restrict>& list_restr )
   {
      list_restr.erase( list_restr.begin() , list_restr.end() );
	  mmap_cacheRestricts::iterator it;
      for( it = cacheRestricts.lower_bound( dk ) ;
           it != cacheRestricts.upper_bound( dk ) ; ++it )
         list_restr.push_back( (*it).second );
   }

protected:
   virtual psDatakind* create_psDatakind( const string& name ) = 0;
   dkMapLst dkLst;   
   mmap_cacheRestricts cacheRestricts;
};


#endif


