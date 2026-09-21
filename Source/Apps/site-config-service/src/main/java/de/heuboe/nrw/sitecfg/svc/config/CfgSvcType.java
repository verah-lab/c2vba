package de.heuboe.nrw.sitecfg.svc.config;

public enum CfgSvcType {
	// ------------------------------------------------------------------------------------------------------------------------------
	ZST, NRW, CHB;
	// ------------------------------------------------------------------------------------------------------------------------------
	static CfgSvcType VALUE = NRW;
	// ------------------------------------------------------------------------------------------------------------------------------
	public static void set( String s ) {
		VALUE = create(s, VALUE);
	}
	public static CfgSvcType get() {
		return VALUE;
	}
	public static boolean isZST() {
		return CfgSvcType.ZST == VALUE;
	}
	public static boolean isNRW() {
		return CfgSvcType.NRW == VALUE;
	}
	public static boolean isCHB() {
		return CfgSvcType.CHB == VALUE;
	}
	// ------------------------------------------------------------------------------------------------------------------------------
	public static CfgSvcType create( String s, CfgSvcType defValue ) {
		CfgSvcType value = CfgSvcType.valueOf(s != null ? s.toUpperCase() : "");
		return value != null ? value : defValue;
	}
	// --------------------------------------------------------------------------------------------
}
