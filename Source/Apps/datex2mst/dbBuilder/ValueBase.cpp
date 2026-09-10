/****************************************************************************/
/*
   Project      TOSCA

   File:        ValueBaseT.cpp


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

#include "ValueBase.h"
#include "tr_err.h"
#include "trMisc.h"

#include "ddp.h"
#include <sstream>



#define get_long_value dynamic_cast<VlongBaseT *>(pt_value)->getValue();
#define get_short_value dynamic_cast<VshortBaseT *>(pt_value)->getValue();




ValueBaseT::ValueBaseT() 
{
  hbPrefixT pref;
  DebugE3(cout << pref << __FUNCTION__ << "()" <<  endl;);

  type = VAL_LONG;
  pt_value = new VBaseSkalarCalcC<long>(0);

  DebugE3(cout << pref << "Ende von " << __FUNCTION__ << endl;);

};


ValueBaseT::ValueBaseT(const ValueBaseT& attr)

{

  hbPrefixT pref;
  DebugE3(cout << pref << __FUNCTION__ << "(ValueBaseT)" <<  endl;);

  type = attr.type;

  string strattr;

  list<ValueBaseT*>::const_iterator iter_value_list;
  ValueBaseT* pt_new_value_val;


  {
	  switch (type)
	  {
		 case VAL_STRING:
		   attr.getValue(strattr);
		   pt_value = new VstringBaseT(strattr);
		   break;
		 case VAL_ATTRIBUTE_PAIR:
		   pt_pair_value.first = 
				   new ValueBaseT( *(attr.pt_pair_value.first) );
		   pt_pair_value.second = 
				   new ValueBaseT( *(attr.pt_pair_value.second) );
		   break;                     
         case VAL_ATTRIBUTE_LIST:
           iter_value_list = attr.pt_list_value.begin();
           while ( iter_value_list != attr.pt_list_value.end() )
		   {
             pt_new_value_val = new  ValueBaseT(*(*iter_value_list)); 
             pt_list_value.push_back( pt_new_value_val );
             ++iter_value_list;
		   };
		   break;
		 case VAL_LONG:
		   long lattr;
		   attr.getValue(lattr);
		   pt_value = new VlongBaseT(lattr);
		   break;
		 case VAL_BOOL:
		   bool battr;
		   attr.getValue(battr);
		   pt_value = new VboolBaseT(battr);
		   break;
		 case VAL_CHAR:
		   char cattr;
		   attr.getValue(cattr);
		   pt_value = new VcharBaseT(cattr);
		   break;
		 case VAL_SHORT:
		   short sattr;
		   attr.getValue(sattr);
		   pt_value = new VshortBaseT(sattr);
		   break;
		 case VAL_DOUBLE:
		   double dattr;
		   attr.getValue(dattr);
		   pt_value = new VdoubleBaseT(dattr);
		   break;
		 default:
		   throw_psErr( TR_Exception::ERR_UNKNOWN_VALUE_TYPE );
	  };
  };  

  DebugE3(cout << pref << "Ende von " << __FUNCTION__ << endl;);

}; 


ValueBaseT::ValueBaseT( const ddpValue& val , const ddpType& type  )

{
   pt_value = 0;    
   pt_pair_value.first = 0;
   pt_pair_value.second = 0;

   switch( type.code() )
   {
      case ddpType::Long:
         operator=( ValueBaseT( val.asLong() ) );
         break;
      case ddpType::Double:
         operator=( ValueBaseT( val.asDouble() ) );
         break;
      case ddpType::Short:
         operator=( ValueBaseT( val.asShort() ) );
         break;
      case ddpType::Byte:
         operator=( ValueBaseT( short( val.asUnsignedChar() ) ) );
         break;
      case ddpType::String:
         operator=( ValueBaseT( val.asString() ) );
         break;
      case ddpType::Boolean:
         operator=( ValueBaseT( val.asBool() ) );
         break;
      default:
         operator=( ValueBaseT( long(0) ) );
   }
};



ValueBaseT::~ValueBaseT()

{

  hbPrefixT pref;
  DebugE3(cout << pref << __FUNCTION__ << "()" <<  endl;);

  if (type == VAL_ATTRIBUTE_PAIR)
  {
     delete pt_pair_value.first;
     delete pt_pair_value.second;
  }
  else if (type == VAL_ATTRIBUTE_LIST)
  {
    list<ValueBaseT*>::const_iterator iter_value_list;
    iter_value_list = pt_list_value.begin();
    while ( iter_value_list != pt_list_value.end() )
    {
       delete *iter_value_list;
       iter_value_list++;
    };
  } 
  else
    delete pt_value; 

  DebugE3(cout << pref << "Ende von " << __FUNCTION__ << endl;);

};


ValueBaseT::ValueBaseT(long value) 
{

  hbPrefixT pref;
  DebugE3(cout << pref << __FUNCTION__ << "(long)" <<  endl;);

  type = VAL_LONG;
  pt_value = new VlongBaseT(value);

  DebugE3(cout << pref << "Ende von " << __FUNCTION__ << endl;);

};



ValueBaseT::ValueBaseT(const pair< long ,long >& pairlongvalue)

{
  hbPrefixT pref;
  DebugE3(cout << pref << __FUNCTION__ << "(string,string)" <<  endl;);

  type = VAL_ATTRIBUTE_PAIR; 

  pt_pair_value.first = new ValueBaseT(pairlongvalue.first);
  pt_pair_value.second = new ValueBaseT(pairlongvalue.second);  

  DebugE3(cout << pref << "Ende von " << __FUNCTION__ << endl;);
}; 


ValueBaseT::ValueBaseT(double value)
{

  hbPrefixT pref;
  DebugE3(cout << pref << __FUNCTION__ << "(double)" <<  endl;);

  type = VAL_DOUBLE; 
  pt_value = new VdoubleBaseT(value);

  DebugE3(cout << pref << "Ende von " << __FUNCTION__ << endl;);

};




ValueBaseT::ValueBaseT(short value)
{

  hbPrefixT pref;
  DebugE3(cout << pref << __FUNCTION__ << "(short)" <<  endl;);

  type = VAL_SHORT; 
  pt_value = new VshortBaseT(value);

  DebugE3(cout << pref << "Ende von " << __FUNCTION__ << endl;);
};


ValueBaseT::ValueBaseT(bool value)
{

  hbPrefixT pref;
  DebugE3(cout << pref << __FUNCTION__ << "(bool)" <<  endl;);

  type = VAL_BOOL; 
  pt_value = new VboolBaseT(value);

  DebugE3(cout << pref << "Ende von " << __FUNCTION__ << endl;);

};

ValueBaseT::ValueBaseT(char value)
{

  hbPrefixT pref;
  DebugE3(cout << pref << __FUNCTION__ << "(char)" <<  endl;);

  type = VAL_CHAR; 
  pt_value = new VcharBaseT(value);

  DebugE3(cout << pref << "Ende von " << __FUNCTION__ << endl;);

};


// Visual C++ compiler warning on bool cast of long

#ifdef WIN32
#define alc_get_bool( l ) ( !l ? false :  true )
#else
#define alc_get_bool( l) ( (bool) l )
#endif


ValueBaseT::ValueBaseT( const string& value )

{

  hbPrefixT pref;
  DebugE3(cout << pref << __FUNCTION__ << "(string,bool)" <<  endl;);

  type = VAL_STRING; 
  pt_value = new VstringBaseT(value);

  DebugE3(cout << pref << "Ende von " << __FUNCTION__ << endl;);

};



ValueBaseT::ValueBaseT( const list<long>& listvalue )
{

  hbPrefixT pref;
  DebugE3(cout << pref << __FUNCTION__ << "(list<long>)" <<  endl;);

  type = VAL_ATTRIBUTE_LIST; 

  list<long>::const_iterator iter_long_list;
  ValueBaseT* pt_item;

  pt_list_value.erase(pt_list_value.begin(),pt_list_value.end());
  iter_long_list = listvalue.begin();
  while ( iter_long_list != listvalue.end() )
  {
    pt_item = new ValueBaseT(*iter_long_list);  
    pt_list_value.push_back(pt_item);
    iter_long_list++;
  };

  DebugE3(cout << pref << "Ende von " << __FUNCTION__ << endl;);

};

ValueBaseT::ValueBaseT(const list<long>& list1, const list<long>& list2 )
{

  hbPrefixT pref;
  DebugE3(cout << pref << __FUNCTION__ << "(list<long>,list<long>)" <<  endl;);

  type = VAL_ATTRIBUTE_PAIR; 
  pt_pair_value.first = new ValueBaseT( list1 );  
  pt_pair_value.second = new ValueBaseT( list2 );  

  DebugE3(cout << pref << "Ende von " << __FUNCTION__ << endl;);

};


ValueBaseT::ValueBaseT(const string& str1,const string& str2)

{

  hbPrefixT pref;
  DebugE3(cout << pref << __FUNCTION__ << "(string,string)" <<  endl;);

  type = VAL_ATTRIBUTE_PAIR; 

  pt_pair_value.first = new ValueBaseT(str1);
  pt_pair_value.second = new ValueBaseT(str2);  

  DebugE3(cout << pref << "Ende von " << __FUNCTION__ << endl;);

}; 


ValueBaseT::ValueBaseT( const pair<short,short>& val )

{
  hbPrefixT pref;
  DebugE3(cout << pref << __FUNCTION__ << "(short,short)" <<  endl;);

  type = VAL_ATTRIBUTE_PAIR; 

  pt_pair_value.first = new ValueBaseT(val.first);
  pt_pair_value.second = new ValueBaseT(val.second);  

  DebugE3(cout << pref << "Ende von " << __FUNCTION__ << endl;);
};






void ValueBaseT::getValue( long& value ) const
{
  if ( type == VAL_LONG )
	  value = dynamic_cast<VlongBaseT *>(pt_value)->getValue();
  else if ( check_val(VAL_LONG) )
   {
      string str = makeString();
      stringstream os; 
      os << str;
	   os >> value;
   } 
}; 


void ValueBaseT::getValue( long& val1, long& val2 ) const

{
  if ( check_val(VAL_ATTRIBUTE_PAIR) )
  {
     if ( pt_pair_value.first->check_val(VAL_LONG) )
        pt_pair_value.first->getValue( val1 );
     if ( pt_pair_value.second->check_val(VAL_LONG) ) 
        pt_pair_value.second->getValue( val2 );
  }
};


void ValueBaseT::getValue( double& value ) const
{
  if ( type == VAL_DOUBLE )
	  value = dynamic_cast<VdoubleBaseT *>(pt_value)->getValue();
  else if (check_val(VAL_DOUBLE))
  {
     string str = makeString();
     stringstream os; 
     os << str;
	  os >> value;
  } 
}; 


void ValueBaseT::getValue( short& value ) const
{
  if ( type == VAL_SHORT )
	  value = dynamic_cast<VshortBaseT *>(pt_value)->getValue();
  else if (check_val(VAL_SHORT))
  {
     string str = makeString();
     stringstream os; 
     os << str;
	  os >> value;
  } 
};


void ValueBaseT::getValue( short& val1, short& val2 ) const

{
  if ( check_val(VAL_ATTRIBUTE_PAIR) )
  {
     if ( pt_pair_value.first->check_val(VAL_SHORT) )
        pt_pair_value.first->getValue( val1 );
     if ( pt_pair_value.second->check_val(VAL_SHORT) ) 
        pt_pair_value.second->getValue( val2 );
  }
};



void ValueBaseT::getValue( string& value ) const

{
  if ( type == VAL_STRING )
	  value = dynamic_cast<VstringBaseT *>(pt_value)->getValue();
  else if (check_val(VAL_STRING))
      value = makeString();
};


void ValueBaseT::getValue( string& val1, string& val2 ) const

{
  if ( check_val(VAL_ATTRIBUTE_PAIR) )
  {
     if ( pt_pair_value.first->check_val(VAL_STRING) )
        val1 = pt_pair_value.first->makeString();
     if ( pt_pair_value.second->check_val(VAL_STRING) ) 
        val2 = pt_pair_value.second->makeString();
  }
};


void ValueBaseT::getValue( bool& value ) const

{
  if ( type == VAL_BOOL )
	  value = dynamic_cast<VboolBaseT *>(pt_value)->getValue();
  else if (check_val(VAL_BOOL))
  {
     string str = makeString();
     stringstream os; 
     os << str;
	  os >> value;
  } 
};

void ValueBaseT::getValue( char& value ) const

{
  if ( type == VAL_CHAR )
	  value = dynamic_cast<VcharBaseT *>(pt_value)->getValue();
  else if (check_val(VAL_CHAR))
  {
     string str = makeString();
     stringstream os; 
     os << str;
	  os >> value;
  } 
};




void ValueBaseT::getValue( list<long>& value1, 
                           list<long>& value2 ) const

{
  if (check_val(VAL_ATTRIBUTE_PAIR))
  {
    if ( pt_pair_value.first->check_val(VAL_ATTRIBUTE_LIST) )
      pt_pair_value.first->getValue( value1 );
    if ( pt_pair_value.second->check_val(VAL_ATTRIBUTE_LIST) ) 
      pt_pair_value.second->getValue( value2 );
  }
};




void ValueBaseT::getValue( list<long>& value ) const

{
  long lvalue;

  if ( check_val(VAL_ATTRIBUTE_LIST) )
  {
    list<ValueBaseT*>::const_iterator iter_longattr_list;
    ValueBaseT* pt_item;

    iter_longattr_list = pt_list_value.begin();

	 if ( pt_list_value.size() > 0 )
	 {
      if ( (*iter_longattr_list)->check_val(VAL_LONG) )
		{
        value.erase(value.begin(),value.end());
        while ( iter_longattr_list != pt_list_value.end() )
		  {
          pt_item = *iter_longattr_list;
          pt_item->getValue(lvalue);
          value.push_back(lvalue);
          iter_longattr_list++;
		  };
		};
	 };
  };
};


void ValueBaseT::getValue( ValueBaseT& val1 , ValueBaseT& val2 )

{
   if ( type != VAL_ATTRIBUTE_PAIR );
  //    throw_psErr( TR_Exception::ERR_VALUE_TYPE_MISMATCH );
   else		 
   {
	   val1 = *(pt_pair_value.first);
	   val2 = *(pt_pair_value.second);
   }
};


void ValueBaseT::getValue( list<ValueBaseT>& vals )

{
   if ( type != VAL_ATTRIBUTE_LIST );
   //   throw_psErr( TR_Exception::ERR_VALUE_TYPE_MISMATCH );
   else		 
   {
      list<ValueBaseT*>::const_iterator it;
      it = pt_list_value.begin();
      while ( it != pt_list_value.end() )
	  {
         vals.push_back( *(*it) );
         it++;
	  };
   }
};


ValueTypeT ValueBaseT::getValueType() const { return type; };



ValueBaseT& ValueBaseT::operator=( const ValueBaseT& attr )

{
 
  string strattr;

  list<ValueBaseT*>::const_iterator iter_attr_list;
  ValueBaseT* pt_new_attr_val;


  if (this == &attr)
    return *this;

  if (type == VAL_ATTRIBUTE_PAIR)
  {
    delete pt_pair_value.first;
    delete pt_pair_value.second;
  }
  else if (type == VAL_ATTRIBUTE_LIST)
  {
    list<ValueBaseT*>::const_iterator iter_attr_list;
    iter_attr_list = pt_list_value.begin();
    while ( iter_attr_list != pt_list_value.end() )
    {
       delete *iter_attr_list;
       iter_attr_list++;
    };
  } 
  else
    delete pt_value;

  type = attr.type;


  switch (type)
  {
     case VAL_ATTRIBUTE_PAIR:
       pt_pair_value.first = 
               new ValueBaseT( *(attr.pt_pair_value.first) );
       pt_pair_value.second = 
               new ValueBaseT( *(attr.pt_pair_value.second) );
       return *this;
     case VAL_ATTRIBUTE_LIST:
       iter_attr_list = attr.pt_list_value.begin();
       while ( iter_attr_list != attr.pt_list_value.end() )
       {
         pt_new_attr_val = new  ValueBaseT(*(*iter_attr_list)); 
         pt_list_value.push_back( pt_new_attr_val );
         ++iter_attr_list;
       };
       return *this;
     case VAL_LONG:
       long lattr;
       attr.getValue(lattr);
       pt_value = new VlongBaseT(lattr);
       return *this;
     case VAL_BOOL:
       bool battr;
       attr.getValue(battr);
       pt_value = new VboolBaseT(battr);
       return *this;
     case VAL_CHAR:
       char cattr;
       attr.getValue(cattr);
       pt_value = new VcharBaseT(cattr);
       return *this;
     case VAL_SHORT:
       short sattr;
       attr.getValue(sattr);
       pt_value = new VshortBaseT(sattr);
       return *this;
     case VAL_STRING:
       attr.getValue(strattr);
       pt_value = new VstringBaseT(strattr);
       return *this;
     case VAL_DOUBLE:
       double dattr;
       attr.getValue(dattr);
       pt_value = new VdoubleBaseT(dattr);
       return *this;
     default:
  	    throw_psErr( TR_Exception::ERR_UNKNOWN_VALUE_TYPE );
	   return *this;
  };

};


bool ValueBaseT::operator<( const ValueBaseT& attr ) const

{

  string str1,str2;

  list<ValueBaseT*>::const_iterator iter1;
  list<ValueBaseT*>::const_iterator iter2;

  if ( type != attr.type )
    return false;


  switch (type)
  {
     case VAL_LONG:
       long v1,v2;
       attr.getValue(v1);
       getValue(v2);
       return ( v1 < v2 );
     case VAL_BOOL:
       bool b1,b2;
       attr.getValue(b1);
       getValue(b2);
       return ( b1 < b2 );
     case VAL_CHAR:
       char c1,c2;
       attr.getValue(c1);
       getValue(c2);
       return ( c1 < c2 );
     case VAL_SHORT:
       short s1,s2;
       attr.getValue(s1);
       getValue(s2);
       return ( s1 < s2 );
     case VAL_STRING:
       attr.getValue(str1);
       getValue(str2);
       return ( str1 < str2 );
     case VAL_DOUBLE:
       double d1,d2;
       attr.getValue(d1);
       getValue(d2);
       return ( d1 < d2 );
     case VAL_ATTRIBUTE_PAIR:
       return (
			     ( *(pt_pair_value.first) < *(attr.pt_pair_value.first) )
			  )
              ||
		      (
			     ( *(pt_pair_value.first) == *(attr.pt_pair_value.first) )
				 &&
				 ( *(pt_pair_value.second) < *(attr.pt_pair_value.second) )
			  );
     case VAL_ATTRIBUTE_LIST:
       iter1 = pt_list_value.begin();
       iter2 = attr.pt_list_value.begin();
       while ( 
               ( iter1 != pt_list_value.end() )
               && 
               ( iter2 != attr.pt_list_value.end() )
             )
       {
          if( *iter1 == *iter2 )
			 {
				 ++iter1;
				 ++iter2; 
				 continue;
			 }
			 else 
             return (*iter1 < *iter2);
       };
       return false;

     default:
        return false;  
  };

};



bool ValueBaseT::operator==( const ValueBaseT& attr ) const

{

  string str1,str2;

  bool ret_val = true;        
  list<ValueBaseT*>::const_iterator iter1;
  list<ValueBaseT*>::const_iterator iter2;

  if ( type != attr.type )
    return false;


  switch (type)
  {
     case VAL_LONG:
       long v1,v2;
       attr.getValue(v1);
       getValue(v2);
       return ( v1 == v2 );
     case VAL_BOOL:
       bool b1,b2;
       attr.getValue(b1);
       getValue(b2);
       return ( b1 == b2 );
     case VAL_CHAR:
       char c1,c2;
       attr.getValue(c1);
       getValue(c2);
       return ( c1 == c2 );
     case VAL_SHORT:
       short s1,s2;
       attr.getValue(s1);
       getValue(s2);
       return ( s1 == s2 );
     case VAL_STRING:
       attr.getValue(str1);
       getValue(str2);
       return ( str1 == str2 );
     case VAL_DOUBLE:
       double d1,d2;
       attr.getValue(d1);
       getValue(d2);
       return ( d1 == d2 );
     case VAL_ATTRIBUTE_PAIR:
       return ( *(pt_pair_value.first) == *(attr.pt_pair_value.first) )
              &&
              ( *(pt_pair_value.second) == *(attr.pt_pair_value.second) );
     case VAL_ATTRIBUTE_LIST:
       iter1 = pt_list_value.begin();
       iter2 = attr.pt_list_value.begin();
       while ( 
               ( iter1 != pt_list_value.end() )
               && 
               ( iter2 != attr.pt_list_value.end() )
             )
       {
         ret_val = ret_val && ( *iter1 == *iter2 );
         ++iter1;
         ++iter2; 
       };
       if ( ( iter1 != pt_list_value.end() )
            ||    
            ( iter2 != attr.pt_list_value.end() )
          )
         ret_val = false;

       return ret_val; 

     default:
       return false;  
  };

};





ValueBaseT ValueBaseT::operator+( 
         const ValueBaseT& attr_val ) const
{
  if ( type != attr_val.type )
     throw_psErr( TR_Exception::ERR_VALUE_TYPE_MISMATCH );

  if (type == VAL_LONG)
  {
     long l1,l2;
     getValue(l1);
     attr_val.getValue(l2);
     return ValueBaseT( l1 + l2 );
  }
  else if ( type == VAL_DOUBLE)
  {
     double d1,d2;
     getValue(d1);
     attr_val.getValue(d2);
     return ValueBaseT( d1 + d2 );
  }

  throw_psErr( TR_Exception::ERR_VALUE_TYPE_MISMATCH );
};



ValueBaseT operator*( long key, const ValueBaseT& attr_val )
{
  if ( attr_val.type == VAL_LONG)
  {
     long l;
     attr_val.getValue(l);
     return ValueBaseT( key * l ); 
  }
  else if ( attr_val.type == VAL_DOUBLE)
  {
     double d;
     attr_val.getValue(d);
     return ValueBaseT( (double)key * d ); 
  }
  throw_psErr( TR_Exception::ERR_VALUE_TYPE_MISMATCH );
};


ValueBaseT operator*( const ValueBaseT& attr_val, long key )
{
   if ( attr_val.type == VAL_LONG)
   {
      long l;
      attr_val.getValue(l);
      return ValueBaseT( l * key ); 
   }
   throw_psErr( TR_Exception::ERR_VALUE_TYPE_MISMATCH );
};




string ValueBaseT::makeString() const

{
  string strattr;

  string ps;
  string ls; 
  string is;

  string retval;


  list<ValueBaseT*>::const_iterator iter_attr_list;

  stringstream os; 


  {
	  switch ( type )
	  {
		 case VAL_ATTRIBUTE_PAIR:
		   ps = "pair( ";
		   ps = ps + pt_pair_value.first->makeString() + " , ";
		   ps = ps + pt_pair_value.second->makeString() + " )";
		   retval = ps;
		   break;
		 case VAL_ATTRIBUTE_LIST:
		   ls = "list( ";
		   iter_attr_list = pt_list_value.begin();
		   while ( iter_attr_list != pt_list_value.end() )
		   {
             is = (*iter_attr_list)->makeString();
			 ++iter_attr_list;
			 if ( iter_attr_list != pt_list_value.end() )
               ls = ls + is + " , " ;               
		     else
			   ls = ls + is;
		   };
           ls = ls + " )";
		   retval = ls;
		   break;
		 case VAL_LONG:
		   long lattr;
		   getValue(lattr);
		   os << lattr;
		   os >> retval;
		   break;
		 case VAL_BOOL:
		   bool battr;
		   getValue(battr);
		   os << battr;
         os >> retval;
		   break;
		 case VAL_CHAR:
		   char cattr;
		   getValue(cattr);
		   os << cattr;
           os >> retval;
		   break;
		 case VAL_SHORT:
		   short sattr;
		   getValue(sattr);
		   os << sattr;
           os >> retval;
		   break;
		 default:
         throw_psErr( TR_Exception::ERR_UNKNOWN_VALUE_TYPE );
		   break;
	  };
  };

  return retval;

};

ostream& operator<<(ostream& os,const ValueBaseT& attr)

{
  hbPrefixT pref;

  string strattr;

  list<ValueBaseT*>::const_iterator iter_attr_list;


  //os << pref << "ValueTypeT: type: " << get_type_str( attr.type ) 
  //   << endl << pref << "value: ";

	  switch ( attr.type )
	  {
		 case VAL_ATTRIBUTE_PAIR:
		   os << "pair:" << endl;
		   os << *(attr.pt_pair_value.first);
		   os << *(attr.pt_pair_value.second);
		   break;
		 case VAL_ATTRIBUTE_LIST:
		   os << "list:" << endl;
		   iter_attr_list = attr.pt_list_value.begin();
		   while ( iter_attr_list != attr.pt_list_value.end() )
		   {
			 os << *(*iter_attr_list);
			 ++iter_attr_list;
		   };
		   break;
		 case VAL_LONG:
		   long lattr;
		   attr.getValue(lattr);
		   os << lattr << endl;
		   break;
		 case VAL_BOOL:
		   bool battr;
		   attr.getValue(battr);
		   os 
#ifdef WIN32
			   << boolalpha 
#endif
			   << battr << endl;
		   break;
		 case VAL_CHAR:
		   char cattr;
		   attr.getValue(cattr);
		   os << cattr << endl;
		   break;
		 case VAL_SHORT:
		   short sattr;
		   attr.getValue(sattr);
		   os << sattr << endl;
		   break;
		 case VAL_STRING:
		   attr.getValue(strattr);
		   os << strattr << endl;
		   break;
		 case VAL_DOUBLE:
		   double dattr;
		   attr.getValue(dattr);
		   os << dattr << endl;
		   break;
		 default:
		   throw_psErr( TR_Exception::ERR_UNKNOWN_VALUE_TYPE );
		   break;
	  };

  os << pref << "end of alertc_attr_val" << endl; 
  return os; 
};  



ValueBaseT maxAttrVal( const ValueBaseT& attr1 ,
				              const ValueBaseT& attr2 )

{
	
  string str1,str2;

  if ( attr1.type != attr2.type )
  {
     throw_psErr( TR_Exception::ERR_MAX_VALUE_DIFF_TYPE );
  }; 


  switch ( attr1.type )
  {
     case VAL_LONG:
       long v1,v2;
       attr1.getValue(v1);
       attr2.getValue(v2);
       return ValueBaseT( max( v1 ,v2 ) );
     case VAL_BOOL:
       bool b1,b2;
       attr1.getValue(b1);
       attr2.getValue(b2);
       return ValueBaseT( b1 || b2 );
     case VAL_CHAR:
       char c1,c2;
       attr1.getValue(c1);
       attr2.getValue(c2);
       return ValueBaseT( max( c1 ,c2 ) );
     case VAL_SHORT:
       short s1,s2;
       attr1.getValue(s1);
       attr2.getValue(s2);
       return ValueBaseT( max( s1 ,s2 ) );
     case VAL_STRING:
       attr1.getValue(str1);
       attr2.getValue(str2);
       return ValueBaseT( max( str1 ,str2 ) );
     case VAL_DOUBLE:
       double d1,d2;
       attr1.getValue(d1);
       attr2.getValue(d2);
       return ValueBaseT( max( d1 ,d2 ) );
     default:
       throw_psErr( TR_Exception::ERR_MAX_VALUE_DIFF_TYPE );
  };
	
};



ValueBaseT minAttrVal( const ValueBaseT& attr1 ,
				              const ValueBaseT& attr2 )

{
	
  string str1,str2;

  if ( attr1.type != attr2.type )
     throw_psErr( TR_Exception::ERR_MAX_VALUE_DIFF_TYPE );

  switch ( attr1.type )
  {
     case VAL_LONG:
       long v1,v2;
       attr1.getValue(v1);
       attr2.getValue(v2);
       return ValueBaseT( min( v1 ,v2 ) );
     case VAL_BOOL:
       bool b1,b2;
       attr1.getValue(b1);
       attr2.getValue(b2);
       return ValueBaseT( b1 && b2 );
     case VAL_CHAR:
       char c1,c2;
       attr1.getValue(c1);
       attr2.getValue(c2);
       return ValueBaseT( min( c1 ,c2 ) );
     case VAL_SHORT:
       short s1,s2;
       attr1.getValue(s1);
       attr2.getValue(s2);
       return ValueBaseT( min( s1 ,s2 ) );
     case VAL_STRING:
       attr1.getValue(str1);
       attr2.getValue(str2);
       return ValueBaseT( min( str1 ,str2 ) );
     case VAL_DOUBLE:
       double d1,d2;
       attr1.getValue(d1);
       attr2.getValue(d2);
       return ValueBaseT( min( d1 ,d2 ) );
     default:
       throw_psErr( TR_Exception::ERR_MAX_VALUE_DIFF_TYPE );
       return ValueBaseT();  
  };
	
};


int ValueBaseT::compare( const ValueBaseT& attr ) 

{
  if ( maxAttrVal( *this , attr ) == attr )
  {
    if ( minAttrVal( *this , attr ) == attr )
      return 0;
    else
      return -1;
  }
  else 
	return 1;   
};


bool ValueBaseT::check_val( ValueTypeT t ) const

{
   bool is_compound = ( t == VAL_ATTRIBUTE_PAIR ) || ( t == VAL_ATTRIBUTE_LIST );
   bool is_compound_this = ( type == VAL_ATTRIBUTE_PAIR ) || 
		                     ( type == VAL_ATTRIBUTE_LIST );	 

   return is_compound == is_compound_this;
};





