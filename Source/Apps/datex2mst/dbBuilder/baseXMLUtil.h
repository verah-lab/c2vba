////////////////////////////////////////////////////////////////////////////////
// 
// (C) Copyright 2005 SHB
// ALL RIGHTS RESERVED
//
// FILE:	baseXMLUtil.h
// AUTHOR:	Peter Schmitz
////////////////////////////////////////////////////////////////////////////////
#ifndef _BASEXMLUTIL_H_
#define _BASEXMLUTIL_H_



#include "hbtimewrap.h"


class __noClass
{
};

template< class = __noClass >
class baseXMLUtil
{
// Helferlein
public:
	hbString UTC2XMLDate( time_t t )
	{
		char buf[101];

		tm* T = hb_localtime( &t );
		if( !T )
			return "";

		strftime( buf, 100, "%Y-%m-%d", T );

		return buf;
	}

	hbString UTC2XMLTime( time_t t )
	{
		char buf[101];

		tm* T = hb_localtime( &t );
		if( !T )
			return "";

		strftime( buf, 100, "%H:%M:%S", T );

		// @@@ !!!
		if( T->tm_isdst )
			return buf + string("+02:00");
		else
			return buf + string("+01:00");
	}

	
#define BASE_FILE_URL	"file://"
#define BASE_HTTP_URL	"http://"

	hbString quoteSpecialCharURI( const hbString& str )
	{
		// Wird eine URI in einem XML-Dokument benutzt, sind Sonderzeichen in der
		// Form '%hh' anzugeben (hh ist die hexadezimale Darstellung des ASCII-Codes).
		// Außerdem ist ggf. das Protokoll-Präfix 'file://' zu ergänzen.

		char* buf = new char[3 * str.size() + 20];
		char* bufPtr = buf;

		if( ( str.substr( 0, 7 ) != BASE_FILE_URL ) && ( str.substr( 0, 7 ) != BASE_HTTP_URL ) )
		{
			strcpy( bufPtr, BASE_FILE_URL );
			bufPtr += 7;
		}

		unsigned i;
		for( i = 0; i < str.size(); i++ )
		{
			char c = str[i];
			if( 
				( ( c >= 'a' ) && ( c <= 'z' ) )
				||
				( ( c >= 'A' ) && ( c <= 'Z' ) )
				||
				( ( c >= '0' ) && ( c <= '9' ) )
				||
				( c == '/' ) ||
				( c == '\\' )
			  )
			{
				*bufPtr = c;
				bufPtr++;
			}
			else
			{
				*bufPtr = '%';
				sprintf( bufPtr+1, "%X", (unsigned char)(c) );

				bufPtr += 3;
			}
		}

		*bufPtr = '\0';

		hbString ret = buf;
		delete [] buf;

		return ret;
	}
	
	hbString quoteSpecialChar( const hbString& str )
	{
		char* buf = new char[6 * str.size() + 1];
		char* bufPtr = buf;

		unsigned i;
		for( i = 0; i < str.size(); i++ )
		{
			if( str[i] == '"' )
			{
				strcpy( bufPtr, "&quot;" );
				bufPtr += 6;
			}
			else if( str[i] == '&' )
			{
				strcpy( bufPtr, "&amp;" );
				bufPtr += 5;
			}
			else if( str[i] == '\\' )
			{
				strcpy( bufPtr, "&apos;" );
				bufPtr += 6;
			}
			else if( str[i] == '<' )
			{
				strcpy( bufPtr, "&lt;" );
				bufPtr += 4;
			}
			else if( str[i] == '>' )
			{
				strcpy( bufPtr, "&gt;" );
				bufPtr += 4;
			}
			else if( str[i] == 0x0D )
			{
				// Zeilenumbrüche durch Leerzeichen ersetzen

				*bufPtr = ' ';
				bufPtr++;

				if( (i+1) < str.size() )
				{
					// Ein folgendes 'line feed' ignorieren
					if( str[i+1] == 0x0A )
						i++;
				}
			}
			else if( str[i] == 0x0A )
			{
				// Zeilenumbrüche durch Leerzeichen ersetzen

				*bufPtr = ' ';
				bufPtr++;
			}
			else if( 
						( str[i] == 'ä' ) || 
						( str[i] == 'ö' ) || 
						( str[i] == 'ü' ) || 
						( str[i] == 'Ä' ) || 
						( str[i] == 'Ö' ) || 
						( str[i] == 'Ü' ) ||
						( str[i] == 'ß' )  
				   )
			{
				*bufPtr = str[i];
				bufPtr++;
			}
			else if( 
				     ( (unsigned char)(str[i]) >= 127 ) ||
				     ( (unsigned char)(str[i]) < 32 )
                   )
			{
				strcpy( bufPtr, "&#" ); 
				sprintf( bufPtr + 2, "%03d", (unsigned)(unsigned char)(str[i]) );
				*(bufPtr+5) = ';';

				bufPtr += 6;
			}
			else
			{
				*bufPtr = str[i];
				bufPtr++;
			}
		}

		*bufPtr = '\0';

		hbString ret = buf;
		delete [] buf;

		return ret;
	}


	string sbTrim( const string& str )
	{
		unsigned i1 = 0;
		int i2 = str.size() - 1;

		while( ( i1 < str.size() ) && ( ( str[i1] == ' ' ) || ( str[i1] == '\t' ) || ( str[i1] == '\0' ) ) )
			i1++;

		while( ( i2 >= 0 ) && ( ( str[i2] == ' ' ) || ( str[i2] == '\t' ) || ( str[i2] == '\0' ) ) )
			i2--;

		if( i2 < int(i1) )
			return "";

		return str.substr( i1, i2 - i1 + 1 );
	}

	bool sbIsTrueString( const hbString& boolVal )
	{
		return ( boolVal == "1" ) || ( hbString( boolVal ).toUpper() == "TRUE" );
	}

	

// Singleton-Implementierung /////////////////////////////////////////////////////////////////

public:
	static baseXMLUtil* get_baseXMLUtil()
	{
		if( !m_instance )
			m_instance = new baseXMLUtil();
		return m_instance;
	}
private:
	baseXMLUtil()
	{
	}
	static baseXMLUtil* m_instance;	
};

template <class C> baseXMLUtil<C>* baseXMLUtil<C>::m_instance = 0;

#define BASE_XML_UTIL (*(baseXMLUtil<>::get_baseXMLUtil()))



#endif
