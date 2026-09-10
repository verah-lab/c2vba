package de.heuboe.datex2.mst.builder;

import de.heuboe.datex2.exception.D2ExceptionBase;

public interface D2MSTPubPostProcessor {

	String process( String content ) throws D2ExceptionBase;
}
