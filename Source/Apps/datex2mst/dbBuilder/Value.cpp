/****************************************************************************/
/*
   Project      TOSCA 

   File:        Value.cpp       


   Lib/Process  TRAILS out
   Author:      Peter Schmitz                  
   Comment:     

   Date        |   Comment
   ----------------------------------------------------
   23.04.1999  |   initial version
               |      

*/   
/****************************************************************************/
#ifdef WIN32
#pragma warning (disable : 4786)
#endif

#include "Value.h"
#include "trMisc.h"
#include <iostream>


#include "ddp.h"

ValueT ValueT::null = ValueT( string("@this is the null value@") );


ostream& operator<<(ostream& os, const ValueT& attr) 

{
  MBPrefixT pref;

  return os << " value: " << attr.value << endl;  
};
  


ValueT maxAttr( const ValueT& val1 , const ValueT& val2 )

{
   return ( ValueT(maxAttrVal( val1.value , val2.value ) ) );
};

ValueT minAttr( const ValueT& val1 , const ValueT& val2 )

{
   return ( ValueT( minAttrVal( val1.value , val2.value ) ) );
};







