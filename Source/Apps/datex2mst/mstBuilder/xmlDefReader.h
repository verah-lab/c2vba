////////////////////////////////////////////////////////////////////////////////
// 
// (C) Copyright 2005 SHB
// ALL RIGHTS RESERVED
//
// FILE:	xmlDefReader.h
// AUTHOR:	Peter Schmitz
////////////////////////////////////////////////////////////////////////////////
#ifndef _XMLDEFREADER_H_
#define _XMLDEFREADER_H_


#include "mstBuilderLib.h"
using namespace mstBuilder;

#include "baseXMLBase.h"
#include "mbMapping.h"


class xmlDefParser : public baseXMLParserIface
{
public:
	virtual bool parse( DOMNode* node );
	virtual hbString getSchemaName() const;
	virtual hbString getSchemaFileName() const;
	static void setSchemaFileName( const hbString& sfn );

	mstDefinition getDefinition()
	{
		return m_definition;
	}

private:
	mstDefinition m_definition;

private:
	static hbString		m_schemaFile;
};

class xmlDefReader
{
public:
	bool parseDef( const hbString& confFileName, hbString& errTxt );

	static xmlDefReader* get_xmlDefReader()
	{
		if( !m_instance )
			m_instance = new xmlDefReader();

		return m_instance;
	}

	void setSchemaFileName( const hbString& sfn );

	static mstDefinition getDefinition()
	{
		return m_definition;
	}

private:
	static mstDefinition m_definition;

private:
	static xmlDefReader* m_instance;
	xmlDefReader() {};
	static xmlDefReader* _instance;
};

#define XML_DEF_READER (*(xmlDefReader::get_xmlDefReader()))



#endif
