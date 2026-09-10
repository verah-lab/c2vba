////////////////////////////////////////////////////////////////////////////////
// 
// (C) Copyright 2008 SHB
// ALL RIGHTS RESERVED
//
// FILE:	xmlDefReader.cpp
// AUTHOR:	Peter Schmitz
////////////////////////////////////////////////////////////////////////////////
#ifdef WIN32
#pragma warning ( disable : 4786 4335 )
#endif


#include "xmlDefReader.h"
#include "baseXMLUtil.h"
#include "mstConst.h"


xmlDefReader* xmlDefReader::m_instance = 0;
mstDefinition xmlDefReader::m_definition;


////////////////////////////////////////////////////////////////////////////////////////////////////
// xmlDefParser
////////////////////////////////////////////////////////////////////////////////////////////////////

hbString xmlDefParser::m_schemaFile = "mstDefinition.xsd";


bool xmlDefParser::parse( DOMNode* node )
{
	baseXMLNode defNode( node );

	m_definition.name = defNode.getChildText("name");
	m_definition.description = defNode.getChildText("description");

	m_definition.lclVersion = defNode.getChildText("lclVersion");

	hbString qNameMode = defNode.getChildText("qNameMode");

	if( !qNameMode.empty() && 
		( qNameMode != Q_NAME_MODE_QKURZ ) && 
		( qNameMode != Q_NAME_MODE_QNAME ) 
	  )
	{
		m_errorText = "Invalid qNameMode <" + qNameMode + "> !";
		return false;
	}

	m_definition.qNameMode = qNameMode.c_str();

	m_definition.active = _xmlBool( defNode.getChildText("active") );
	
	baseXMLNode ces =  defNode.getChild( "columnEntries" );
	baseXMLNodeLst items =  ces.getChildren( "item" );

	m_definition.columnEntries.length( items.size() );

	baseXMLNodeLst::iterator itd;
	unsigned i = 0;
	for( itd = items.begin(); itd != items.end(); ++itd, i++ )
	{
		baseXMLNode& N_ce = *itd;

		m_definition.columnEntries[i].datakindName = N_ce.getChildText("datakindName");
		m_definition.columnEntries[i].column = N_ce.getChildText("column");

		m_definition.columnEntries[i].columnCharacteristic = N_ce.getChildText("columnCharacteristic");
		m_definition.columnEntries[i].validInterval = N_ce.getChildText("validInterval").asLong();
		m_definition.columnEntries[i].period = N_ce.getChildText("period").asLong();
		m_definition.columnEntries[i].filterColumn = N_ce.getChildText("filterColumn");

		list<hbString> fvs = N_ce.getChildrenText("filterValue");
		m_definition.columnEntries[i].filterValue.length( fvs.size() );
		
		list<hbString>::iterator itfv;
		unsigned j = 0;
		for( itfv = fvs.begin(); itfv != fvs.end(); ++itfv, j++ )
		{
			m_definition.columnEntries[i].filterValue[j] = *itfv;
		}
	}

	baseXMLNode N_filter = defNode.getChild("filter");

	if( N_filter.valid() )
	{
		baseXMLNode N_ofs = N_filter.getChild( "objFilters" );

		if( N_ofs.valid() )
		{
			baseXMLNodeLst items =  N_ofs.getChildren( "item" );

			m_definition.filter.objFilters.length( items.size() );
			unsigned k = 0;
			for( itd = items.begin(); itd != items.end(); ++itd, k++ )
			{
				baseXMLNode& N_item = *itd;
				m_definition.filter.objFilters[k].id = N_item.getChildText("id");

				baseXMLNode sois = N_item.getChild( "subObjIds" );
				if( sois.valid() )
				{
					 list<hbString> sons =  sois.getChildrenText( "item" );
					 m_definition.filter.objFilters[k].subObjIds.item.length( sons.size() );
					 unsigned l = 0;
					 list<hbString>::iterator itn;
					 for( itn = sons.begin(); itn != sons.end(); ++itn, l++ )
					 {
						 m_definition.filter.objFilters[k].subObjIds.item[l] = *itn;
					 }
				}

				baseXMLNode N_name = N_item.getChild( "name" );
				if( N_name.valid() )
					m_definition.filter.objFilters[k].name = N_name.getText();

				baseXMLNode N_referenceType = N_item.getChild( "referenceType" );
				if( N_referenceType.valid() )
					m_definition.filter.objFilters[k].referenceType = N_referenceType.getText();

				baseXMLNode N_type = N_item.getChild( "type" );
				if( N_type.valid() )
					m_definition.filter.objFilters[k].type = N_type.getText();

				_baseHbValue ex = N_item["excluded"];
				if( ex.isEmpty() )
					m_definition.filter.objFilters[k].excluded = ex.asBool();
			}
		}

		baseXMLNode N_rfs = N_filter.getChild( "roadFilters" );

		if( N_rfs.valid() )
		{
			items =  N_rfs.getChildren( "item" );
			m_definition.filter.roadFilters.length( items.size() );
			unsigned k = 0;
			for( itd = items.begin(); itd != items.end(); ++itd, k++ )
			{
				baseXMLNode& N_item = *itd;
				m_definition.filter.roadFilters[k].name = N_item.getChildText("name");
				m_definition.filter.roadFilters[k].startOffset = N_item.getChildText("startOffset").asLong();
				m_definition.filter.roadFilters[k].endOffset = N_item.getChildText("endOffset").asLong();
			}
		}
	}
	return true;
}

hbString xmlDefParser::getSchemaName() const
{
	return "http://www.heuboe.de/mst/definition";
}

hbString xmlDefParser::getSchemaFileName() const
{
	return m_schemaFile;
}

void xmlDefParser::setSchemaFileName( const hbString& sfn ) 
{
	m_schemaFile = sfn;
}



////////////////////////////////////////////////////////////////////////////////////////////////////
// xmlDefReader
////////////////////////////////////////////////////////////////////////////////////////////////////


static inline bool _isText( const hbString& xml )
{
	hbString _T = BASE_XML_UTIL.sbTrim( xml );
	return ( _T.size() && _T[(hbString::size_type)0] == '<' );
}

bool xmlDefReader::parseDef( const hbString& definition, hbString& errTxt )
{
	xmlDefParser* parser = new xmlDefParser();
	
	baseXMLParser P( parser );

	hbString error;
	if( !BASE_XML.XMLParse( definition, P, errTxt ) )
	{
		HbErr( errTxt );
		return false;
	}

	m_definition = parser->getDefinition();

	return true;
}

void xmlDefReader::setSchemaFileName( const hbString& sfn )
{
	xmlDefParser::setSchemaFileName( sfn );
}
