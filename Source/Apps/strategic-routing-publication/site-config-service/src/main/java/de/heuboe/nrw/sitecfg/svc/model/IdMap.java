package de.heuboe.nrw.sitecfg.svc.model;

import java.util.Collection;
import java.util.TreeMap;
import java.util.function.Function;

@SuppressWarnings("serial")
public class IdMap<T> extends TreeMap<String, T> {
	public IdMap() {
		super();
	}
	public IdMap( Collection<T> coll, Function<T, String> idGetter ) {
		coll.forEach(value -> put(idGetter.apply(value), value));
	}
	public boolean has( String id ) {
		return containsKey(id);
	}
}
