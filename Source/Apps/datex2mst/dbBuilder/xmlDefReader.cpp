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
#include "d2Conf.h"
#include "baseXMLUtil.h"


xmlDefReader* xmlDefReader::m_instance = 0;


////////////////////////////////////////////////////////////////////////////////////////////////////
// xmlDefParser
////////////////////////////////////////////////////////////////////////////////////////////////////

hbString xmlDefParser::m_schemaFile = "MSTDef.xsd";


bool xmlDefParser::parse( DOMNode* node )
{
	m_objNameFilter.clear();
	m_roadFilter.clear();

	baseXMLNode mstNode( node );

	hbString name = mstNode["name"];
	unsigned version = mstNode["version"].asUnsignedInt();

	baseXMLNode descrNode = mstNode.getChild( "Description" );
	hbString descr = descrNode.getText();

	m_mst = d2MST( name, descr, version );

	baseXMLNodeLst roadFilterNodes =  mstNode.getChildren( "RoadFilter" );

	baseXMLNodeLst::iterator itrf;
	for( itrf = roadFilterNodes.begin(); itrf != roadFilterNodes.end(); ++itrf )
	{
		baseXMLNode& N = *itrf;

		long startOffset	= N["startOffset"];
		long endOffset		= N["endOffset"];

		hbString roadName	= N.getText();
		hbString dir		= N["direction"];

		if( !dir.size() )
		{
			HbErr( "!!! Fehler in xmlDefParser::parse()" << endl <<
				   "Kein Richtung fuer '" << roadName << "' !" )
			return false;
		}

		m_roadFilter.push_back( d2RoadFilter( roadName, dir[(unsigned)0], startOffset, endOffset ) );
	}


	baseXMLNodeLst objNameFilterNodes =  mstNode.getChildren( "ObjNameFilter" );
	hbStringLst laneIds;

	baseXMLNodeLst::iterator itof;
	for( itof = objNameFilterNodes.begin(); itof != objNameFilterNodes.end(); ++itof )
	{
		baseXMLNode& N = *itof;

		hbString name = N.getChild("Name").getText();
		laneIds = N.getChildTextLst( "LaneId" );

		m_objNameFilter.push_back( d2ObjNameFilter( name, laneIds ) );
	}


	baseXMLNodeLst datakindItemNodes = mstNode.getChildren( "DatakindItem" );

	baseXMLNodeLst::iterator itdi;
	for( itdi = datakindItemNodes.begin(); itdi != datakindItemNodes.end(); ++itdi )
	{
		baseXMLNode& N = *itdi;
		
		hbString	datakindName			= N.getChild("DatakindName").getText();
		hbString	datakindIdCol			= N.getChild("DatakindIdCol").getText();
		hbString	datakindValueCol		= N.getChild("DatakindValueCol").getText();
		hbString	datakindStateCol		= N.getChild("DatakindStateCol").getText();

		hbStringLst datakindInvalidVals;
		baseXMLNodeLst nodes = N.getChildren("DatakindInvalidVal"); 
		baseXMLNodeLst::iterator iti;
		for( iti = nodes.begin(); iti != nodes.end(); ++iti )
		{
			baseXMLNode& N_ = *iti;
			datakindInvalidVals.push_back( N_.getText() );
		}

		hbString	vehicleClass			= N.getChild("VehicleClass").getText();
		hbString	valueUnit				= N.getChild("ValueUnit").getText();
		long		validInterval			= atol( N.getChild("ValidInterval").getText().c_str() );

		m_dkAttrDefLst.push_back( d2DkAttrDef( datakindName, datakindIdCol,
			                                   datakindValueCol, datakindStateCol,
											   datakindInvalidVals, vehicleClass,
											   valueUnit, validInterval ) );
	}

	return true;
}

hbString xmlDefParser::getSchemaName() const
{
	return "http://www.heuboe.de/d2defMST";
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


static bool _getDefAsStr( const hbString& fn, hbString& xml, hbString& errTxt )
{
	xml.reserve( 256 * 1024 );
	FILE *f = fopen( fn, "r" );

	if (!f)
	{
		errTxt = "Datei '" + fn + "' kann nicht geöffnet werden";
		return false;
	}

	char buf[1024];
	while (fgets( buf, 1024, f)) 
	{
		xml += buf;
	}
	fclose( f );

	return true;
}


static inline bool _isText( const hbString& xml )
{
	hbString _T = BASE_XML_UTIL.sbTrim( xml );
	return ( _T.size() && _T[(hbString::size_type)0] == '<' );
}

bool xmlDefReader::parseDef( const hbString& defintion, d2MST& mst, d2RoadFilterLst& roadFilter, 
							 d2ObjNameFilterLst& objNameFilter, d2DkAttrDefLst& dkAttrDefLst, 
							 hbString& errTxt )
{
	xmlDefParser* parser = new xmlDefParser();
	
	baseXMLParser P( parser );

	hbString error;
	if( !BASE_XML.XMLParse( defintion, P, errTxt ) )
	{
		HbErr( errTxt );
		return false;
	}

	mst				= parser->getMst();

	hbString def = defintion;

	if( !_isText( defintion ) )
	{
		if( !_getDefAsStr( defintion, def, errTxt ) )
		{
			HbErr( "!!! Fehler in xmlDefReader::parseConfig() !!!" ) 
			HbErr( errTxt ) 

			return false;
		}
	}

	mst.setDef( def );

	roadFilter		= parser->getRoadFilter();
	objNameFilter	= parser->getObjNameFilter();
	dkAttrDefLst	= parser->getDkAttrDefLst();

	return true;
}

void xmlDefReader::setSchemaFileName( const hbString& sfn )
{
	xmlDefParser::setSchemaFileName( sfn );
}
