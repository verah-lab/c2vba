package de.heuboe.nrw.sitecfg.svc.config;

public enum CreationMode {
	NONE, FILE, SOAP;
	public static CreationMode create( String s ) {
		CreationMode value = CreationMode.valueOf(s != null ? s.toUpperCase() : "");
		return value != null ? value : CreationMode.NONE;
	}
}
