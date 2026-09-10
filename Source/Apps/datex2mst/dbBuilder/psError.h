#ifndef _PSERROR_H_
#define _PSERROR_H_



#include <vector>
#include "hbError.h"



struct errTextStruct
{
   char* textL1;
   char* textL2;
};

struct psErrorExplText
{
  long id;
  errTextStruct texts;   
};

class psErrorT : public hbException

{
public:   
   psErrorT( const char* whatString) :
     hbException( whatString ) {};
   psErrorT( const errSource& sRef,const char* scope,
   			    long errCode,const char* errText) :
     hbException( sRef,scope,errCode,errText) {};
   psErrorT( const errSource& sRef,
   			    long errCode,const char* errText) :
     hbException( sRef,"",errCode,errText) {};
   psErrorT( const hbError& cause,const errSource& sRef,
   			 const char* scope,long errCode,const char* errText) :
     hbException( cause,sRef,scope,errCode,errText) {};

   static void extendErrorSet( int num , psErrorExplText* ext )
   {
      for( int i = 0; i < num ; i++ )
         extErr.push_back( ext + i );
   };   

   static const char* get_errText( long errCode )
   {
      for ( unsigned i = 0 ; i < extErr.size() ; i++ )   
      {
         if( extErr[i]->id == errCode )
            return extErr[i]->texts.textL1;
	  }
      return "";
   };

private:
   static vector<psErrorExplText*> extErr;
};

#define rnErrorT psErrorT
#define rnErrorExplText psErrorExplText


#define throw_rnErr( errCode ) \
        throw ( psErrorT( _src_ , "" , errCode , \
                          psErrorT::get_errText( errCode ) \
                        ) \
              );

#define throw_psErr( errCode ) \
        throw ( psErrorT( _src_ , "" , errCode , \
                          psErrorT::get_errText( errCode ) \
                        ) \
              );

#define BiLangErrorExpl( firstStr , secondStr ) (firstStr)

#define _ERR_SRC_ __FILE__ + string(", ") + __FUNCTION__ + ": " + __LINE__

#endif













