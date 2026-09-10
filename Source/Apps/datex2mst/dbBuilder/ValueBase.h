/****************************************************************************/
/*
   Project      TOSCA

   File:        ValueBaseT.h


   Lib/Process  TRAILS out
   Author:      Peter Schmitz                  
   Comment:     

   Date        |   Comment
   ----------------------------------------------------
   23.04.1999  |   initial version
               |      

*/   
/****************************************************************************/
#ifndef _VALUEBASE_H_
#define _VALUEBASE_H_

#include <string>
#include <list>
#include "VBase.h"
//#include <strstream>




enum ValueTypeT
{
   VAL_LONG,
   VAL_SHORT,
   VAL_DOUBLE,
   VAL_BOOL,
   VAL_CHAR,
   VAL_STRING,
   VAL_ATTRIBUTE_LIST,
   VAL_ATTRIBUTE_PAIR
};


class ddpValue;
class ddpType;

class ValueBaseT

{

public:
   ValueBaseT(); 
   ValueBaseT(const ValueBaseT& val); 
   ValueBaseT( const ddpValue& val , const ddpType& type );
   ~ValueBaseT();

   ValueBaseT(long value); 
   ValueBaseT(double value);
   ValueBaseT(short value); 
   ValueBaseT( const pair<short,short>& val );

   ValueBaseT(const string& value);
   ValueBaseT(bool value); 
   ValueBaseT(char value); 

   ValueBaseT(const list<long>& listvalue); 
   ValueBaseT(const pair< long ,long >& pairlongvalue); 
   ValueBaseT(const list<long>& list1, const list<long>& list2 );
   ValueBaseT(const string& str1,const string& str2);
 

   void getValue( long& value ) const;
   void getValue( long& val1, long& val2 ) const;
   void getValue( double& value ) const;
   void getValue( short& value ) const;
   void getValue( short& val1, short& val2 ) const;
   void getValue( string& value ) const;
   void getValue( string& val1, string& val2 ) const;
   void getValue( bool& value ) const;
   void getValue( char& value ) const;

   void getValue( list<long>& value ) const;
   void getValue( list<long>& value1, list<long>& value2 ) const;

	void getValue( ValueBaseT& val1 , ValueBaseT& val2 );
	void getValue( list<ValueBaseT>& vals );

   ValueBaseT& operator=( const ValueBaseT& val );
   bool operator==( const ValueBaseT& val ) const;
   bool operator<( const ValueBaseT& val ) const;
   int compare( const ValueBaseT& val ) ;

   ValueBaseT operator+( const ValueBaseT& val ) const;
   friend ValueBaseT operator*( long key, const ValueBaseT& val );
   friend ValueBaseT operator*( const ValueBaseT& val, long key );

   friend ValueBaseT maxAttrVal( const ValueBaseT& val1 ,  
                                 const ValueBaseT& val2 );

   friend ValueBaseT minAttrVal( const ValueBaseT& val1 ,  
                                 const ValueBaseT& val2 );

   bool check_val( ValueTypeT type ) const;

   ValueTypeT getValueType() const;

   friend ostream& operator<<(ostream& os, const ValueBaseT& val);  
   string makeString() const;


private:

   ValueTypeT type;   
   VBaseT* pt_value;    

   list<ValueBaseT*> pt_list_value;
   pair<ValueBaseT*,ValueBaseT*> pt_pair_value;

   // compound variants

   ValueBaseT* getFirstListValue 
      (list<ValueBaseT*>::const_iterator&  iter_pt_list_value) const;
   ValueBaseT* getNextListValue 
      (list<ValueBaseT*>::const_iterator&  iter_pt_list_value) const;
};



#endif








