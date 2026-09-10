#ifndef _PSITERATOR_H_
#define _PSITERATOR_H_


#include <list>
using namespace std;

#include "hb_auto_ptr.h"

template<class item>
class psIterator

{
public:
   psIterator( const psIterator<item>& dit ) 
	{
		elem = dit.elem; 
      it = dit.it;
	}

	virtual ~psIterator() {};

	psIterator<item>& operator=( const psIterator<item>& dit ) 
	{
		if( this == &dit )
			return *this;

		elem = dit.elem; 
      it = dit.it;
		return *this;
	}

	bool operator==( const psIterator<item>& dit ) const
	{
      if( end() && dit.end() )
			return true;
      else if( ( !end() && dit.end() ) || ( end() && !dit.end() ) )
			return false;
      else
			return (it == dit.it);
	}

   virtual void get_next() { ++it; } 
   virtual bool ready() { return !end(); } 
	virtual item get_item() { return *it; };

   virtual bool end() const = 0; 
	virtual void reset() = 0; 
protected:
	psIterator() {}
	typename list<item>::const_iterator it; 
};



template<class item>
class nc_psIterator

{
public:
   nc_psIterator( const nc_psIterator<item>& dit ) 
	{
		elem = dit.elem; 
      it = dit.it;
	}

	virtual ~nc_psIterator() {};

	nc_psIterator<item>& operator=( const nc_psIterator<item>& dit ) 
	{
		if( this == &dit )
			return *this;

		elem = dit.elem; 
      it = dit.it;
		return *this;
	}

	bool operator==( const nc_psIterator<item>& dit ) const
	{
      if( end() && dit.end() )
			return true;
      else if( ( !end() && dit.end() ) || ( end() && !dit.end() ) )
			return false;
      else
			return (it == dit.it);
	}

   virtual void get_next() { ++it; } 
   virtual bool ready() { return !end(); } 
	virtual item get_item() { return *it; };

   virtual bool end() const = 0; 
	virtual void reset() = 0; 
protected:
	nc_psIterator() {}
	typename list<item>::iterator it; 
};

#define ASSIGN_SAFE_POINTER( var, type, code ) \
   auto_ptr<type> ps_temp_auto_ptr( code ); \
	var = ps_temp_auto_ptr.get();

#define NEW_SAFE_POINTER( var, type, code ) \
   type* var;\
   auto_ptr<type> ps_temp_auto_ptr( code ); \
	var = ps_temp_auto_ptr.get();


#endif
