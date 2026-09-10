////////////////////////////////////////////////////////////////////////////////
// 
// (C) Copyright 2008 SHB
// ALL RIGHTS RESERVED
//
// FILE:	xmlConfReader.cpp
// AUTHOR:	Peter Schmitz
////////////////////////////////////////////////////////////////////////////////
#ifdef WIN32
#pragma warning ( disable : 4786 4335 )
#endif


#include "xmlConfReader.h"
#include "baseXMLUtil.h"


xmlConfReader* xmlConfReader::m_instance = 0;
mbDatakindMappings xmlConfReader::m_datakindMappings;


////////////////////////////////////////////////////////////////////////////////////////////////////
// xmlConfParser
////////////////////////////////////////////////////////////////////////////////////////////////////

hbString xmlConfParser::m_schemaFile = "mstConfig.xsd";


bool xmlConfParser::parse( DOMNode* node )
{
	baseXMLNode mstNode( node );

	baseXMLNodeLst datakindMappings =  mstNode.getChildren( "datakindMapping" );

	baseXMLNodeLst::iterator itd;
	for( itd = datakindMappings.begin(); itd != datakindMappings.end(); ++itd )
	{
		mbDatakindMapping mbdm;
		mbDatakind		  mbdk;	

		baseXMLNode& N_dkm = *itd;

		baseXMLNode N_dkm_dk = N_dkm.getChild( "datakindDesc" );

		mbdk.m_name = N_dkm_dk.getChildText("datakind");
		mbdk.m_description = N_dkm_dk.getChildText("description");
		mbdk.m_referenceType = toReferenceTypeEnum(N_dkm_dk.getChildText("referenceType"));
		mbdk.m_equipment = N_dkm_dk.getChildText("equipment");
		mbdk.m_objectType = N_dkm_dk.getChildText("objectType");
		mbdk.m_subTypes = N_dkm_dk.getChildrenText("subType");
		mbdk.m_idColumn = N_dkm_dk.getChildText("idColumn");

		baseXMLNodeLst N_dkm_dk_cols = N_dkm_dk.getChildren( "column" );
		baseXMLNodeLst::iterator itc;
		for( itc = N_dkm_dk_cols.begin(); itc != N_dkm_dk_cols.end(); ++itc )
		{
			baseXMLNode N_dkm_dk_col = *itc;
			mbColumn C;
			C.m_valColumn = N_dkm_dk_col.getChildText("valColumn");
			C.m_valColumnCharacteristic = N_dkm_dk_col.getChildText("valColumnCharacteristic");
			C.m_valColumnDescription = N_dkm_dk_col.getChildText("valColumnDescription");
			C.m_valColumnCharacteristicDescription = N_dkm_dk_col.getChildText("valColumnCharacteristicDescription");
			C.m_validColumn = N_dkm_dk_col.getChildText("validColumn");
			C.m_invalidValues = N_dkm_dk_col.getChildrenText("invalidValue");
			C.m_defaultValidInterval = (unsigned)( N_dkm_dk.getChildText("defaultValidInterval").asLong() );

			mbdk.addColumn( C );
		}

		mbdm.m_datakind = mbdk;


		baseXMLNodeLst N_dkm_ms = N_dkm.getChildren( "mapping" );

		baseXMLNodeLst::iterator itd;
		for( itd = N_dkm_ms.begin(); itd != N_dkm_ms.end(); ++itd )
		{
			baseXMLNode N_dkm_m = *itd;

			baseXMLNode N_in = N_dkm_m.getChild("in");
			baseXMLNode N_mm = N_dkm_m.getChild("mappingType");
			baseXMLNode N_mn = N_dkm_m.getChild("mappingName");
			baseXMLNode N_md = N_dkm_m.getChild("mappingData");
			baseXMLNode N_f = N_dkm_m.getChild("factor");
			baseXMLNode N_vdt = N_dkm_m.getChild("valueDataType");
			baseXMLNode N_out = N_dkm_m.getChild("out");

			mbMapping mapping;

			if( N_f.valid() && !N_f.getText().empty() )
				mapping.m_factor						= N_f.getText().asDouble();
			else
				mapping.m_factor						= 1.0;

			if( N_f.valid() && !N_f.getText().empty() )
				mapping.m_valueDataType					= N_vdt.getText();
			else
				mapping.m_valueDataType					= "integer";

			mapping.m_mappingType					= toMappingTypeEnum( N_mm.getText() );

			if( mapping.m_mappingType == mbFunction )
			{
				if( !N_mn.valid() || N_mn.getText().empty() )
				{
					m_errorText = "Incomplete mapping function definition !";
					return false;
				}

				if( !N_md.valid() || N_md.getText().empty() )
				{
					m_errorText = "Incomplete mapping function definition !";
					return false;
				}

				mapping.m_mappingFunction			= N_mn.getText() + "[" + N_md.getText() + "]";
			}

			mapping.m_src.m_valColumn				= N_in.getChildText("valColumn");
			mapping.m_src.m_valColumnCharacteristic	= N_in.getChildText("valColumnCharacteristic");

			mapping.m_trg.m_valueType				= toMeasuredOrDerivedDataTypeEnum( N_out.getChildText("valueType") );
			mapping.m_trg.m_vehType					= toVehicleTypeEnum( N_out.getChildText("vehType") );
			mapping.m_trg.m_basicDataType			= N_out.getChildText("basicDataType");
			mapping.m_trg.m_valueElement			= N_out.getChildText("valueElement");
			mapping.m_trg.m_value					= N_out.getChildText("value");
			mapping.m_trg.m_valueInner				= N_out.getChildText("valueInner");
			mapping.m_trg.m_valuePath				= N_out.getChildText("valuePath");

			mbdm.addMapping( mapping );
		}

		m_datakindMappings.addDkMapping( mbdm );
	}


	return true;
}

hbString xmlConfParser::getSchemaName() const
{
	return "http://www.heuboe.de/mst/config";
}

hbString xmlConfParser::getSchemaFileName() const
{
	return m_schemaFile;
}

void xmlConfParser::setSchemaFileName( const hbString& sfn ) 
{
	m_schemaFile = sfn;
}



////////////////////////////////////////////////////////////////////////////////////////////////////
// xmlConfReader
////////////////////////////////////////////////////////////////////////////////////////////////////


static inline bool _isText( const hbString& xml )
{
	hbString _T = BASE_XML_UTIL.sbTrim( xml );
	return ( _T.size() && _T[(hbString::size_type)0] == '<' );
}

bool xmlConfReader::parseDef( const hbString& defintion, hbString& errTxt )
{
	xmlConfParser* parser = new xmlConfParser();
	
	baseXMLParser P( parser );

	hbString error;
	if( !BASE_XML.XMLParse( defintion, P, errTxt ) )
	{
		HbErr( errTxt );
		return false;
	}

	m_datakindMappings = parser->getDatakindMappings();


	return true;
}

void xmlConfReader::setSchemaFileName( const hbString& sfn )
{
	xmlConfParser::setSchemaFileName( sfn );
}
