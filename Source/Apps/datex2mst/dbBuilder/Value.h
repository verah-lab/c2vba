/****************************************************************************/
/*
   Project      TOSCA

   File:        Value.h


   Lib/Process  TRAILS out
   Author:      Peter Schmitz                  
   Comment:     

   Date        |   Comment
   ----------------------------------------------------
   23.04.1999  |   initial version
               |      

*/   
/****************************************************************************/
#ifndef _VALUE_H_
#define _VALUE_H_

#include <string>
#include <list>
#include <iostream>
#include "ValueBase.h"


class ddpValue;
class ddpType;

class ValueT

{
public:
  ValueT() : value( (long)0 ) {};
  ValueT(const ValueT& val ) : value( val.value ) {};
  ValueT(const ValueBaseT& val ) : value( val ) {};

  ValueT( const ddpValue& val , const ddpType& type ) : value( val, type ) {};

  ValueT( long val ) : value( val ) {};
  ValueT( double val ) : value( val ) {};
  ValueT( short val ) : value( val ) {};
  ValueT( const pair<short,short>& val ) : value( val ) {};
  ValueT( const string& val ) : value( val ) {};
  ValueT( const string& val1, const string& val2 ) : value( val1, val2 ) {};
  ValueT( bool val ) : value( val ) {};
  ValueT( char val ) : value( val ) {};
  ValueT( const list<long>& val ) : value( val ) {};
  ValueT( const list<long>& val1, const list<long>& val2 ) : value( val1 , val2  ) {};

  ValueT& operator=(const ValueT& attr) { value = attr.value; return *this; };
  bool operator==( const ValueT& attr ) const 
  { return ( value == attr.value ); };
  bool operator<( const ValueT& attr ) const 
  { return ( value < attr.value ); };


  // getValue methods

  void getValue( long& v ) const { value.getValue( v ); };
  void getValue( long& val1, long& val2 ) const 
  { value.getValue( val1 , val2 ); };
  void getValue( double& v ) const { value.getValue( v ); };
  void getValue( short& v ) const { value.getValue( v ); };
  void getValue( short& val1, short& val2 ) const 
  { value.getValue( val1 , val2 ); };
  void getValue( string& v ) const { value.getValue( v ); };
  void getValue( string& val1, string& val2 ) const 
  { value.getValue( val1 , val2 ); };
  void getValue( bool& v ) const { value.getValue( v ); };
  void getValue( char& v ) const { value.getValue( v ); };
  void getValue( list<long>& v ) const { value.getValue( v ); };
  void getValue( list<long>& val1, list<long>& val2 ) const 
  { value.getValue( val1 , val2 ); };

  void getValue( ValueT& val1 , ValueT& val2 )  
  {
     ValueBaseT vb1;
     ValueBaseT vb2;
	 value.getValue( vb1 , vb2 );
	 val1 = ValueT( vb1 );
	 val2 = ValueT( vb2 );
  }

  void getValue( list<ValueT>& vals )
  {
     vals.erase( vals.begin() , vals.end() );
     list<ValueBaseT> valsB;
     value.getValue( valsB );
     list<ValueBaseT>::iterator it;
     for( it = valsB.begin() ; it != valsB.end() ; ++it )
        vals.push_back( ValueT( *it ) ); 
  }

  friend ValueT maxAttr( const ValueT& attr1 , const ValueT& attr2 );
  friend ValueT minAttr( const ValueT& attr1 , const ValueT& attr2 );

  ValueTypeT getValueType() const { return value.getValueType(); };
  bool hasSimpleType() 
  { return ( getValueType() != VAL_ATTRIBUTE_LIST ) && 
           ( getValueType() != VAL_ATTRIBUTE_PAIR ); };

  friend ostream& operator<<(ostream& os, const ValueT& attr);  

  static ValueT null;

private:
  ValueBaseT value;             
};


#endif







