#ifndef I_ZD_H
#define I_ZD_H

//#include "avlTree44.h"
//#include <cbString.h>
#include "string.h"

//parse temp
class ZDString {
	friend class SymbolTable;
	private:
		char *string;
	public:
		ZDString () { string = 0; }
		ZDString (char *s) { string = s; }
		//ZDString (const cbString &s) { string = strdup (s.getStr ()); }
		inline int operator < (const ZDString& other) const
		{ return strcmp ( string, other.string ) < 0 ? 1 : 0; }
		inline int operator == (const ZDString& other) const
		{ return strcmp ( string, other.string ) == 0 ? 1 : 0; }
		inline int operator > (const ZDString& other) const
		{ return strcmp ( string, other.string ) > 0 ? 1 : 0; }
		operator const char * () { return (string); }
};

#endif
