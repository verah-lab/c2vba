////////////////////////////////////////////////////////////////////////////////
// 
// (C) Copyright 2005 SHB
// ALL RIGHTS RESERVED
//
// FILE:	baseXMLBase.h
// AUTHOR:	Peter Schmitz
////////////////////////////////////////////////////////////////////////////////
#ifndef _BASEXMLBASE_H_
#define _BASEXMLBASE_H_



#include "hbRefCount.h"
#include "hbError.h"
#include "hbLog.h"
#include "hbValue.h"

#include "baseXMLConfig.h"


#include <iostream>

#include <xercesc/dom/DOMErrorHandler.hpp>
#include <xercesc/util/XMLString.hpp>
#include <xercesc/dom/DOMNode.hpp>
#include <xercesc/dom/DOMBuilder.hpp>
XERCES_CPP_NAMESPACE_USE




class __charPtr 
{
public:
	__charPtr( char* ptr ) : m_str( ptr ) {}
	virtual ~__charPtr() { XMLString::release( &m_str ); }
protected:
	char* m_str;
};

class CharPtrWrapper : public __charPtr, public hbString 
{
public:
	CharPtrWrapper( char* str ) : __charPtr( str ), hbString( str )  {}
    CharPtrWrapper( const XMLCh* const str );
};



class __XMLChPtr 
{
public:
	__XMLChPtr( XMLCh* ptr ) : m_str( ptr ) {}
	virtual ~__XMLChPtr() { XMLString::release( &m_str ); }
protected:
	XMLCh* m_str;
};

class XMLChPtrWrapper : public __XMLChPtr
{
public:
	XMLChPtrWrapper( XMLCh* str ) : __XMLChPtr( str )  {}
    XMLChPtrWrapper( const char* str ) : __XMLChPtr( XMLString::transcode( str ) ) {};
    XMLChPtrWrapper( const hbString& str ) : __XMLChPtr( XMLString::transcode( str.c_str() ) ) {};

	operator XMLCh*() const { return m_str; }
};



///////////////////////////////////////////////////////////////////////////////////////
// baseXMLNode
///////////////////////////////////////////////////////////////////////////////////////

class _baseHbValue : public hbValue
{
public:
	_baseHbValue( const hbValue V ) : hbValue( V ) {}
	operator hbString () const
	{
		return asString();
	}
	
	bool asBool() const;
};



class baseXMLNode;
typedef list<baseXMLNode> baseXMLNodeLst;

typedef DOMNode* DOMNodePtr;

class baseXMLNode
{
public:
	baseXMLNode() : m_node( 0 ) {}
	baseXMLNode( DOMNode* node ) : m_node( node ) {}

	bool valid() const 
	{ 
		return ( m_node != 0 ); 
	}
	
	operator DOMNodePtr() const	{ return m_node; }

	// Gibt Text eines Knotens zurück
	hbString getText() const;
	list<hbString> getTextLst() const;

	// Gibt für einen Knoten-Text vom XML-Typ 'list' Einzel-Wert-Liste zurück
	void getValues( list<hbString>& vals ) const;
	void getValues( list<long>& vals ) const;

	// Attribut-Werte
	_baseHbValue operator[]( const hbString& attr ) const;
	
	// Gibt Attribut-Liste eines Attributs vom XML-Typ 'list' zurück
	void getValues( const hbString& attr, list<hbString>& vals ) const;
	void getValues( const hbString& attr, list<long>& vals ) const;

	// Kind-Knoten
	baseXMLNode getChild( const hbString& name ) const;
	baseXMLNodeLst getChildren( const hbString& name ) const;

	hbString getChildText( const hbString& name ) const;
	list<hbString> getChildTextLst( const hbString& name ) const;

	list<hbString> getChildrenText( const hbString& name ) const;
private:
	DOMNode* m_node;
};



///////////////////////////////////////////////////////////////////////////////////////
// baseParseErrorHandler
///////////////////////////////////////////////////////////////////////////////////////

class baseParseErrorHandler : public DOMErrorHandler
{
public:
    baseParseErrorHandler();
    ~baseParseErrorHandler();

    bool getSawErrors() const
	{
	   return m_sawErrors;
	}

	hbString getErrorText() const
	{
		return m_errorText;
	}

    bool handleError(const DOMError& domError);
    void resetErrors();
private :
	hbString m_errorText;
    bool m_sawErrors;
};



class baseXMLParserIface : public hbRefCounted
{
public:
	virtual bool		parse( DOMNode* node ) = 0;
	virtual hbString	getSchemaName() const = 0;
	virtual hbString	getSchemaFileName() const = 0;
public:
	hbString			getErrorText() const { return m_errorText; }
protected:
	hbString m_errorText;
};

typedef hbReference<baseXMLParserIface> baseXMLParser;



class baseXMLSystem 
{
public:
	~baseXMLSystem() 
	{
		XMLClose();
	}

	bool XMLInit( const string& localeStr = "" );
	void XMLClose();
	bool XMLParse( const hbString& xml, baseXMLParser parser, hbString& error );
	bool _XMLParse( const hbString& xml, baseXMLParser parser, hbString& error );

// Schema-Location 
public:
	hbString getXMLConfPath() const 
	{ 
		return BASE_XML_CONF_MAN.getXMLConfPath(); 
	}
private:
	void _setSchemaLocation( const hbString& schemaName, const hbString& schemaFileName );


// Helferlein
public:
	static time_t XMLDateTime2UTC( const hbString& dt );
	static hbString UTC2XMLDate( time_t t );
	static hbString UTC2XMLTime( time_t t );
	
	static bool	  XMLIsTrueValue( const hbValue& boolVal );
	static bool   XMLIsTrueString( const hbString& boolVal );


private:
	baseParseErrorHandler m_errorHandler;
	DOMBuilder* m_parser;
	

// Singleton-Implementierung /////////////////////////////////////////////////////////////////

public:
	static baseXMLSystem* get_baseXMLSystem()
	{
		if( !m_instance )
			m_instance = new baseXMLSystem();
		return m_instance;
	}
private:
	baseXMLSystem() : m_parser(0) 
	{
		XMLInit( "de_DE" );
	}
	static baseXMLSystem* m_instance;	
};

#define _BASE_XML_SYSTEM (*(baseXMLSystem::get_baseXMLSystem()))
#define BASE_XML			(*(baseXMLSystem::get_baseXMLSystem()))
#define _xmlBool( STR ) baseXMLSystem::XMLIsTrueString( STR )




#endif
