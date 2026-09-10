////////////////////////////////////////////////////////////////////////////////
// 
// (C) Copyright 2005 SHB
// ALL RIGHTS RESERVED
//
// FILE:	xmlConfReader.h
// AUTHOR:	Peter Schmitz
////////////////////////////////////////////////////////////////////////////////
#ifndef _XMLCONFREADER_H_
#define _XMLCONFREADER_H_


#include "baseXMLBase.h"
#include "mbMapping.h"


class xmlConfParser : public baseXMLParserIface
{
public:
	virtual bool parse( DOMNode* node );
	virtual hbString getSchemaName() const;
	virtual hbString getSchemaFileName() const;
	static void setSchemaFileName( const hbString& sfn );

	mbDatakindMappings getDatakindMappings()
	{
		return m_datakindMappings;
	}

private:
	mbDatakindMappings	m_datakindMappings;

private:
	static hbString		m_schemaFile;
};

class xmlConfReader
{
public:
	bool parseDef( const hbString& confFileName, hbString& errTxt );

	static xmlConfReader* get_xmlConfReader()
	{
		if( !m_instance )
			m_instance = new xmlConfReader();

		return m_instance;
	}

	void setSchemaFileName( const hbString& sfn );


	static mbDatakindMappings getDatakindMappings()
	{
		return m_datakindMappings;
	}

private:
	static mbDatakindMappings	m_datakindMappings;

private:
	static xmlConfReader* m_instance;
	xmlConfReader() {};
	static xmlConfReader* _instance;
};

#define XML_CONF_READER (*(xmlConfReader::get_xmlConfReader()))



#endif
