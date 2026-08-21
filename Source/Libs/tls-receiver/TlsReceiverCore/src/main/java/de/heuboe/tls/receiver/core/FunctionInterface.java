package de.heuboe.tls.receiver.core;

import java.util.Map;

import de.heuboe.tls.receiver.interfaces.DataItem;

public interface FunctionInterface {
        DataItem eval( String name, Map<String, DataItem> etelVars );
}
