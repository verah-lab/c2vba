package de.heuboe.nrw.sitecfg.svc.config;

public enum DstSystem {
	// ------------------------------------------------------------------------------------------------------------------------------
	NONE, KOMOD, NRW, ASF, BY, NI, TH;
	// ------------------------------------------------------------------------------------------------------------------------------
	static DstSystem VALUE = NRW;
	// ------------------------------------------------------------------------------------------------------------------------------
	public static void set( String s ) {
		VALUE = create(s);
	}
	public static DstSystem get() {
		return VALUE;
	}
	public static boolean isKOMOD() {
		return DstSystem.KOMOD == VALUE;
	}
	public static boolean isNRW() {
		return DstSystem.NRW == VALUE;
	}
	public static boolean isASF() {
		return DstSystem.ASF == VALUE;
	}
	public static boolean isBY() {
		return DstSystem.BY == VALUE;
	}
	public static boolean isNI() {
		return DstSystem.NI == VALUE;
	}
	public static boolean isTH() {
		return DstSystem.TH == VALUE;
	}
	// ------------------------------------------------------------------------------------------------------------------------------
	public static DstSystem create( String s ) {
		DstSystem value = DstSystem.valueOf(s != null ? s.toUpperCase() : "");
		return value != null ? value : DstSystem.KOMOD;
	}
	// --------------------------------------------------------------------------------------------
}
