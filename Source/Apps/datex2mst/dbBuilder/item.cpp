#include "item.h"


ostream& operator<<( ostream& os, const item& I )
{
	os << "Item" << "[" << endl;

	map< hbString, hbValue >::const_iterator it;
	
	for( it = I.m_values.begin(); it != I.m_values.end(); ++it )
	{
		hbString code = it->first;
		os << code + hbString( 25 - code.size(), ' ' ) << ": " << it->second << endl;
	}

	os << "]" << endl;

	return os;
}
