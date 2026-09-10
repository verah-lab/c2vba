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


#include "d2Conf.h"
#include "baseXMLBase.h"


class xmlDefParser : public baseXMLParserIface
{
public:
	virtual bool parse( DOMNode* node );
	virtual hbString getSchemaName() const;
	virtual hbString getSchemaFileName() const;
	static void setSchemaFileName( const hbString& sfn );

	d2MST				getMst() const				{ return m_mst; }
	d2RoadFilterLst		getRoadFilter() const 		{ return m_roadFilter; }
	d2ObjNameFilterLst	getObjNameFilter() const	{ return m_objNameFilter; }	
	d2DkAttrDefLst		getDkAttrDefLst() const		{ return m_dkAttrDefLst; }
private:
	d2MST				m_mst;
	d2RoadFilterLst		m_roadFilter;
	d2ObjNameFilterLst	m_objNameFilter;
	d2DkAttrDefLst		m_dkAttrDefLst;

	static hbString		m_schemaFile;
};

class xmlDefReader
{
public:
	bool parseDef( const hbString& confFileName, d2MST& mst, d2RoadFilterLst& roadFilter, 
		           d2ObjNameFilterLst& objNameFilter, d2DkAttrDefLst& dkAttrDefLst,
				   hbString& errTxt );

	static xmlDefReader* get_xmlDefReader()
	{
		if( !m_instance )
			m_instance = new xmlDefReader();

		return m_instance;
	}

	void setSchemaFileName( const hbString& sfn );


private:
	static xmlDefReader* m_instance;
	xmlDefReader() {};
	static xmlDefReader* _instance;
};

#define XML_DEF_READER (*(xmlDefReader::get_xmlDefReader()))



#endif
