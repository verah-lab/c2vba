////////////////////////////////////////////////////////////////////////////////
// 
// (C) Copyright 2005 SHB
// ALL RIGHTS RESERVED
//
// FILE:	baseXMLBase.cpp
// AUTHOR:	Peter Schmitz
////////////////////////////////////////////////////////////////////////////////
#ifdef WIN32
#pragma warning ( disable : 4786 4335 )
#endif

#include <xercesc/util/PlatformUtils.hpp>
#include <xercesc/parsers/AbstractDOMParser.hpp>
#include <xercesc/dom/DOMImplementation.hpp>
#include <xercesc/dom/DOMImplementationLS.hpp>
#include <xercesc/dom/DOMImplementationRegistry.hpp>
#include <xercesc/dom/DOMException.hpp>
#include <xercesc/dom/DOMDocument.hpp>
#include <xercesc/dom/DOMNodeList.hpp>
#include <xercesc/dom/DOMError.hpp>
#include <xercesc/dom/DOMLocator.hpp>
#include <xercesc/dom/DOMNamedNodeMap.hpp>
#include <xercesc/dom/DOMAttr.hpp>
#include <xercesc/dom/DOMBuilder.hpp>
#include <xercesc/framework/Wrapper4InputSource.hpp>
#include <xercesc/framework/MemBufInputSource.hpp>
#include <xercesc/util/XMLUniDefs.hpp>


#include "baseXMLBase.h"
#include "baseXMLUtil.h"



#include <string>
#include <stdlib.h>
#include <fstream>

using namespace std;


///////////////////////////////////////////////////////////////////////////////////////////
// CharPtrWrapper
///////////////////////////////////////////////////////////////////////////////////////////

CharPtrWrapper::CharPtrWrapper( const XMLCh* const str ) : 
		__charPtr( XMLString::transcode( str ) ), hbString( m_str )  
{
};


///////////////////////////////////////////////////////////////////////////////////////////
// baseXMLNode
///////////////////////////////////////////////////////////////////////////////////////////

bool _baseHbValue::asBool() const
{
	return _xmlBool( asString() );
}


hbString baseXMLNode::getText() const
{
	return CharPtrWrapper( m_node->getTextContent() );
}

list<hbString> baseXMLNode::getTextLst() const
{
	list<hbString> vals;
	CharPtrWrapper( m_node->getTextContent() ).split( " ", vals, true );
	return vals;
}

_baseHbValue baseXMLNode::operator[]( const hbString& attrName ) const
{
	DOMNamedNodeMap* attrs = m_node->getAttributes();
	unsigned i;
	for( i = 0; i < attrs->getLength(); i++ )
	{
		DOMNode* attr = attrs->item( i );

        CharPtrWrapper name( attr->getLocalName() );

		if( name == attrName )
			return hbValue( CharPtrWrapper( attr->getTextContent() ) );
	}

	return hbValue();
}


void baseXMLNode::getValues( list<hbString>& vals ) const
{
	CharPtrWrapper( m_node->getTextContent() ).split( " ", vals, true );
}

void baseXMLNode::getValues( list<long>& vals ) const
{
	vals.clear();

	list< hbString > _values;
	CharPtrWrapper( m_node->getTextContent() ).split( " ", _values, true );

	list< hbString >::iterator it;
	for( it = _values.begin(); it != _values.end(); ++it )
		vals.push_back( atol( it->c_str() ) );
}


void baseXMLNode::getValues( const hbString& attrName, list<hbString>& vals ) const
{
	vals.clear();

	DOMNamedNodeMap* attrs = m_node->getAttributes();
	unsigned i;
	for( i = 0; i < attrs->getLength(); i++ )
	{
		DOMNode* attr = attrs->item( i );

        CharPtrWrapper name( attr->getLocalName() );

		if( name == attrName )
		{
			hbString valStr( CharPtrWrapper( attr->getTextContent() ) );
			valStr.split( " ", vals, true );
		}
	}
}

void baseXMLNode::getValues( const hbString& attrName, list<long>& vals ) const
{
	vals.clear();

	DOMNamedNodeMap* attrs = m_node->getAttributes();
	unsigned i;
	for( i = 0; i < attrs->getLength(); i++ )
	{
		DOMNode* attr = attrs->item( i );

        CharPtrWrapper name( attr->getLocalName() );

		if( name == attrName )
		{
			hbString valStr( CharPtrWrapper( attr->getTextContent() ) );
			list< hbString > _values;
			valStr.split( " ", _values, true );

			list< hbString >::iterator it;
			for( it = _values.begin(); it != _values.end(); ++it )
				vals.push_back( atol( it->c_str() ) );
		}
	}
}


baseXMLNode baseXMLNode::getChild( const hbString& childName ) const
{
    DOMNode* child;
    for( child = m_node->getFirstChild(); child != 0; child = child->getNextSibling() )
	{
		DOMNode::NodeType nt = DOMNode::NodeType( child->getNodeType() );

		if( nt == DOMNode::ELEMENT_NODE )
		{
            CharPtrWrapper name( XMLString::transcode(child->getLocalName()) );

			if( name == childName )
				return baseXMLNode( child );
		}
	}

	return baseXMLNode();
}

baseXMLNodeLst baseXMLNode::getChildren( const hbString& childName ) const
{
	baseXMLNodeLst Lst;

    DOMNode* child;
    for( child = m_node->getFirstChild(); child != 0; child = child->getNextSibling() )
	{
		DOMNode::NodeType nt = DOMNode::NodeType( child->getNodeType() );

		if( nt == DOMNode::ELEMENT_NODE )
		{
            CharPtrWrapper name( XMLString::transcode(child->getLocalName()) );

			if( name == childName )
				Lst.push_back( baseXMLNode( child ) );
		}
	}

	return Lst;
}

hbString baseXMLNode::getChildText( const hbString& childName ) const
{
    DOMNode* child;
    for( child = m_node->getFirstChild(); child != 0; child = child->getNextSibling() )
	{
		DOMNode::NodeType nt = DOMNode::NodeType( child->getNodeType() );

		if( nt == DOMNode::ELEMENT_NODE )
		{
            CharPtrWrapper name( XMLString::transcode(child->getLocalName()) );

			if( name == childName )
				return CharPtrWrapper( child->getTextContent() );
		}
	}

	return hbString();
}

list<hbString> baseXMLNode::getChildTextLst( const hbString& childName ) const
{
	hbString childText = getChildText( childName );

	list<hbString> vals;
	childText.split( " ", vals, true );
	return vals;
}


list<hbString> baseXMLNode::getChildrenText( const hbString& childName ) const
{
	list<hbString> Lst;

    DOMNode* child;
    for( child = m_node->getFirstChild(); child != 0; child = child->getNextSibling() )
	{
		DOMNode::NodeType nt = DOMNode::NodeType( child->getNodeType() );

		if( nt == DOMNode::ELEMENT_NODE )
		{
            CharPtrWrapper name( XMLString::transcode(child->getLocalName()) );

			if( name == childName )
				Lst.push_back( CharPtrWrapper( child->getTextContent() ) );
		}
	}

	return Lst;
}




///////////////////////////////////////////////////////////////////////////////////////////
// baseXMLSystem
///////////////////////////////////////////////////////////////////////////////////////////

baseXMLSystem* baseXMLSystem::m_instance = 0;	


void baseXMLSystem::_setSchemaLocation( const hbString& schemaName, 
									   const hbString& schemaFileName )
{
	//if( getXMLConfPath() != "" )
	//{
		hbString attrStr = schemaName + " " + 
						   // getXMLConfPath() + "/" + 
			               schemaFileName;

		m_parser->setProperty( XMLUni::fgXercesSchemaExternalSchemaLocation, 
							   XMLChPtrWrapper( attrStr ) );
	//}
}

bool baseXMLSystem::XMLInit( const string& localeStr )
{
    // XML4C-System initialisieren
    try
    {
        if( localeStr.size() )
        {
            XMLPlatformUtils::Initialize( localeStr.c_str() );		// ###
        }
        else
        {
            XMLPlatformUtils::Initialize();
        }
    }
    catch (const XMLException& toCatch)
    {
         cerr << "Error during initialization! :\n"
              << CharPtrWrapper(toCatch.getMessage()) << XERCES_STD_QUALIFIER endl;
         return false;
    }

	static const XMLCh gLS[] = { chLatin_L, chLatin_S, chNull };
	DOMImplementation *impl = DOMImplementationRegistry::getDOMImplementation(gLS);
	m_parser = ((DOMImplementationLS*)impl)->createDOMBuilder(DOMImplementationLS::MODE_SYNCHRONOUS, 0);

    m_parser->setFeature(XMLUni::fgDOMNamespaces, true );
    m_parser->setFeature(XMLUni::fgXercesSchema, true);
    m_parser->setFeature(XMLUni::fgXercesSchemaFullChecking, true);
    m_parser->setFeature(XMLUni::fgDOMValidateIfSchema, true);

    // ### Datatype-Normalisierung aktivieren
    m_parser->setFeature(XMLUni::fgDOMDatatypeNormalization, true);

    // Fehler-Handler anlegen
    m_parser->setErrorHandler( &m_errorHandler );

	return true;
}

void baseXMLSystem::XMLClose()
{
    m_parser->release();

    // call the termination method
    XMLPlatformUtils::Terminate();
}

bool baseXMLSystem::XMLParse( const hbString& xml, baseXMLParser parser, hbString& error )
{
	m_parser->resetDocumentPool();
    m_errorHandler.resetErrors();

	return _XMLParse( xml, parser, error );
}



static inline bool _isText( const hbString& xml )
{
	hbString _T = BASE_XML_UTIL.sbTrim( xml );
	return ( _T.size() && _T[(hbString::size_type)0] == '<' );
}

bool baseXMLSystem::_XMLParse( const hbString& xml, baseXMLParser parser, hbString& error )
{
    XERCES_CPP_NAMESPACE::DOMDocument *doc = 0;

	try
	{
		if( _isText( xml ) )
		{
			// xml ist XML-Text
			static const XMLCh noID[] = { chNull };
			MemBufInputSource* mbis = new MemBufInputSource( (const unsigned char * const)(xml.c_str()), xml.size(), noID );
			Wrapper4InputSource w4is( mbis );

			_setSchemaLocation( parser->getSchemaName(), parser->getSchemaFileName() );
			doc = m_parser->parse( w4is );
		}
		else
		{
			// xml ist ein Datei-Name
			_setSchemaLocation( parser->getSchemaName(), parser->getSchemaFileName() );
			doc = m_parser->parseURI( xml );
		}
	}
    catch (const XMLException& toCatch)
    {
		error = string("XMLException: '") + CharPtrWrapper(toCatch.getMessage())+ "'"; 

        HbErr( "XMLException during parsing: '" << endl << 
               "Exception message is:  " << endl << 
               CharPtrWrapper(toCatch.getMessage()) << endl )
		return false;
    }
    catch (const DOMException& toCatch)
    {
        const unsigned int maxChars = 2047;
        XMLCh errText[maxChars + 1];

		hbString msg;
        if( DOMImplementation::loadDOMExceptionMsg(toCatch.code, errText, maxChars) )
			msg = CharPtrWrapper(errText);

		error = string("DOMException: Code: ") + toCatch.code + ", Msg: " + msg;

        HbErr( "DOM Error during parsing: " << endl << 
               "DOMException-Code:  " << toCatch.code << endl <<
               "Message: " << msg << endl )

		return false;
    }
    catch (...)
    {
		error = "Unexpected exception during parsing";
        HbErr( error )
		return false;
    }


    if( m_errorHandler.getSawErrors() )
    {
		error = m_errorHandler.getErrorText();
		return false;
    }
    else
    {
        if( doc ) 
		{
            if( !parser->parse( (DOMNode*)doc->getDocumentElement() ) )
			{
				error = parser->getErrorText();
				return false;
			}
			else
				return true;
        }

		return false;
    }
}



// Helferlein /////////////////////////////////////////////////////////////

time_t baseXMLSystem::XMLDateTime2UTC( const hbString& dt )
{
	if( dt.size() < 19 )
	{
		HbErr( "!!! Fehler in baseXMLSystem::XMLDateTime2UTC() !!!" << endl << 
			   "Parameter '" << dt << "' ungueltig !" )
		return 0;
	}

	tm T;
	T.tm_year = atoi( dt.substr( 0, 4 ).c_str() ) - 1900;
	T.tm_mon  = atoi( dt.substr( 5, 2 ).c_str() ) - 1;
	T.tm_mday = atoi( dt.substr( 8, 2 ).c_str() );

	T.tm_hour = atoi( dt.substr( 11, 2 ).c_str() );
	T.tm_min  = atoi( dt.substr( 14, 2 ).c_str() );
	T.tm_sec  = atoi( dt.substr( 17, 2 ).c_str() );

	T.tm_isdst = -1;

	return hb_mktime( &T );
}

bool baseXMLSystem::XMLIsTrueValue( const hbValue& boolVal )
{
	if( boolVal.isEmpty() )
		return false;

	return XMLIsTrueString( boolVal.asString() );
}

bool baseXMLSystem::XMLIsTrueString( const hbString& boolVal )
{
	return BASE_XML_UTIL.sbIsTrueString( boolVal );
}




/////////////////////////////////////////////////////////////////////////////////////////////
// baseParseErrorHandler
/////////////////////////////////////////////////////////////////////////////////////////////

baseParseErrorHandler::baseParseErrorHandler() : m_sawErrors( false )
{
}

baseParseErrorHandler::~baseParseErrorHandler()
{
}

bool baseParseErrorHandler::handleError(const DOMError& domError)
{
    m_sawErrors = true;

    if( domError.getSeverity() == DOMError::DOM_SEVERITY_WARNING )
		{HbErr( "Warning beim Parsen: " );}
    else if( domError.getSeverity() == DOMError::DOM_SEVERITY_ERROR )
		{HbErr( "Fehler beim Parsen: " );}
    else
		{HbErr( "Schwerwiegender Fehler beim Parsen: " );}

	m_errorText = string("line: ") + domError.getLocation()->getLineNumber() + ", " +
				  "char: " + domError.getLocation()->getColumnNumber() + ":\n" + 
				  CharPtrWrapper(domError.getMessage());
	
    HbErr( CharPtrWrapper( domError.getLocation()->getURI()) << 
		   ", line " << domError.getLocation()->getLineNumber() <<
           ", char " << domError.getLocation()->getColumnNumber() << endl <<  
           "Message: " << CharPtrWrapper(domError.getMessage()) << endl )

    return true;
}

void baseParseErrorHandler::resetErrors()
{
    m_sawErrors = false;
}



//////////////////////////////////////////////////////////////////////////////////////
// baseXMLConfig
//////////////////////////////////////////////////////////////////////////////////////

baseXMLConfig* baseXMLConfig::m_instance = 0;
