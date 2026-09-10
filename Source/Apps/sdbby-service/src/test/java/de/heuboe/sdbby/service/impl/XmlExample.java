package de.heuboe.sdbby.service.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import de.heuboe.sdbby.service.config.data.ConfigItem;
import de.heuboe.sdbby.service.config.data.ConfigSet;


public class XmlExample {


	@Test
	public void test() {
		ConfigSet configSet = new ConfigSet();
		ConfigItem item;
		
		item = new ConfigItem("SBA_A9_Fischbach.DE0", new ArrayList<>());
		item.getId().add("2000311");
		configSet.getConfigItem().add(item);
		
		item = new ConfigItem("WWW München", new ArrayList<>());
		item.getId().add("2000416");
		item.getId().add("2000417");
		item.getId().add("2000386");
		item.getId().add("2000420");
		item.getId().add("2000427");
		configSet.getConfigItem().add(item);

		new CommStatMan().saveConfigSetXML(configSet, "config.xml");
		configSet = new CommStatMan().readConfigSetXML("config.xml");
		assertEquals(2, configSet.getConfigItem().size());
	}


}
