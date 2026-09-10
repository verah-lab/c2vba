#ifdef WIN32
#pragma warning (disable : 4786)
#endif

#include "tr_err.h"
#include "ddpmap.h"
#include "handler.h"

bool psObj::getValue( const string& inp , const ValueT& key ,
                      const string& outp , ValueT& val )

{
   if( operator[]( inp ) == key )
   {
      val = operator[]( outp ); 
      return !(val == ValueT::null);
   }
   return false;
};



void psDK_fact::add_item( const string& dk , const string& col )

{
   hbPrefixT pref;

   DebugG1( cout << pref << "psDK_fact: add cache item (" << dk << "," << col 
			     << ")" << endl; );
   psDatakind* pdk = get_psDatakind( dk );

   if( !pdk )
   {
		hbString errStr = "psDK_fact::add_item(): fatal internal error";
		HbErr( "!!! " << errStr << " !!!" << endl )
			
		list<string> errTexts;
		errTexts.push_back( errStr );
		throwErrorMsg( errTexts );
   }

   list<string> lst;
   lst.push_back( col );

   list<cache_restrict> list_restr;
   get_cache_restrs( dk , list_restr );

   pdk->init( lst , list_restr );
};


void psDK_fact::add_item( const list< str_pair >& cols )

{
   list< str_pair >::const_iterator it;
   for( it = cols.begin() ; it != cols.end() ; ++it )
      add_item( (*it).str1 , (*it).str2 );
};


psObj::errCodes psDK_fact::getValue( const list<long>& IDs ,
                                     const loc_access& accPath ,
                                     map<long,long>& values )

{
   list<long>::const_iterator it;   
   for( it = IDs.begin() ; it != IDs.end() ; ++it )
   {
      long id = *it;
      
	  loc_access::iterator itl( &accPath );
      itl.reset();
      ValueT val;
      while( !itl.end() )
	  {
         access_item item = itl.get_item(); 

		 DebugG3( cout << "datakind: " << item.datakind << endl  
					   << "col in:   " << item.col_in << endl
					   << "val in:   " << id << endl
					   << "col out:  " << item.col_out << endl;
                   );

         if( getValue( item.datakind , item.col_in , ValueT( id ) ,
                       item.col_out , val ) != psObj::OK )
         {
		    DebugG2( cout << "could not retrieve value for " << *it << endl 
					      << "datakind: " << item.datakind << endl  
						  << "col in:   " << item.col_in << endl
						  << "val in:   " << id << endl
						  << "col out:  " << item.col_out << endl;
                   );
            break;
         };
      
         val.getValue( id ); 
         itl.get_next();
	  }

      if( itl.end() )
	  {
		  long v;
		  val.getValue( v );

          values[ *it ] = v;
	  }
   }

   return psObj::OK;
};

psObj::errCodes psDK_fact::getValue( long id_in ,
                                     const loc_access& accPath ,long& lval )

{
   long id = id_in;

   loc_access::iterator itl( &accPath );
   itl.reset();
   ValueT val;
   while( !itl.end() )
   {
      access_item item = itl.get_item(); 

	  DebugG3( cout << "datakind: " << item.datakind << endl  
					<< "col in:   " << item.col_in << endl
					<< "val in:   " << id << endl
			        << "col out:  " << item.col_out << endl << endl;
             );

	  psObj::errCodes err = getValue( item.datakind , item.col_in , 
                                      ValueT( id ) , item.col_out , val );
      if( err != psObj::OK )
      {
		 DebugG2( cout << "could not retrieve value for " << id << endl 
					   << "datakind: " << item.datakind << endl  
					   << "col in:   " << item.col_in << endl
					   << "val in:   " << id << endl
				       << "col out:  " << item.col_out << endl << endl;
                );
         return err;
      };
      
      val.getValue( id ); 
      itl.get_next();

   }

   lval = id;
   return psObj::OK;
    
};



psObj::errCodes psDK_fact::getValue( long id_in ,
                                     const loc_access& accPath ,
                                     const string& dk ,
                                     const string col ,
                                     long& lval )

{
   long id = id_in;

   loc_access::iterator itl( &accPath );
   itl.reset();
   ValueT val;
   while( !itl.end() )
   {
      access_item item = itl.get_item(); 

	  DebugG3( cout << "datakind: " << item.datakind << endl  
					<< "col in:   " << item.col_in << endl
					<< "val in:   " << id << endl
			        << "col out:  " << item.col_out << endl << endl;
             );

      psObj::errCodes err;

      // if datakind dk has been reached get value for column col
      // and break
      if( item.datakind == dk )
	  {
	     psObj::errCodes err = getValue( item.datakind , item.col_in , 
                                         ValueT( id ) , col , val );
		 if( err != psObj::OK )
		 {
		    DebugG2( cout << "could not retrieve value for " << id << endl 
					      << "datakind: " << item.datakind << endl  
					      << "col in:   " << item.col_in << endl
					      << "val in:   " << id << endl
				          << "col out:  " << item.col_out << endl << endl;
                   );
            return err;
         };

         val.getValue( lval );         
         return psObj::OK;
	  }

      else
	     err = getValue( item.datakind , item.col_in , 
                         ValueT( id ) , item.col_out , val );
      if( err != psObj::OK )
      {
		 DebugG2( cout << "could not retrieve value for " << id << endl 
					   << "datakind: " << item.datakind << endl  
					   << "col in:   " << item.col_in << endl
					   << "val in:   " << id << endl
				       << "col out:  " << item.col_out << endl << endl;
                );
         return err;
      };

      itl.get_next();
      val.getValue( id ); 

   }

   return psObj::NO_VALUE;
    
};

