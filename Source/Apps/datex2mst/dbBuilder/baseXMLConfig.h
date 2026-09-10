////////////////////////////////////////////////////////////////////////////////
// 
// (C) Copyright 2005 SHB
// ALL RIGHTS RESERVED
//
// FILE:	baseXMLConfig.h
// AUTHOR:	Peter Schmitz
////////////////////////////////////////////////////////////////////////////////
#ifndef _BASEXMLCONFIG_H_
#define _BASEXMLCONFIG_H_



#include "hbString.h"



class baseXMLConfig 
{
// Schema-Location 
public:
	// §§§ aus Prozess-Aufruf-Parameter setzen
	void setXMLConfPath( const hbString& path ) { m_schemaPath = path; }
	hbString getXMLConfPath() const { return m_schemaPath; }
private:
	hbString m_schemaPath;


// Singleton-Implementierung /////////////////////////////////////////////////////////////////

public:
	static baseXMLConfig* get_baseXMLConfig()
	{
		if( !m_instance )
			m_instance = new baseXMLConfig();
		return m_instance;
	}
private:
	baseXMLConfig() {}
	static baseXMLConfig* m_instance;	
};

#define BASE_XML_CONF_MAN (*(baseXMLConfig::get_baseXMLConfig()))



#endif
