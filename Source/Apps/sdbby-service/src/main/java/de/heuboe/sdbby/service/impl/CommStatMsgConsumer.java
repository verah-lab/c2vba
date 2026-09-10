package de.heuboe.sdbby.service.impl;

import java.util.List;

import eu.vmis_ehe.vmis2.tls.received.SYSFehlerDUE;

public interface CommStatMsgConsumer {

	void update( List<SYSFehlerDUE> fehlerList );
	void stateReceived();
}
