package de.heuboe.nrw.sitecfg.svc.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.heuboe.sitecfg.grpc.CfgType;

@SuppressWarnings({ "unchecked", "serial" })
public class TypeIdObjMap extends TreeMap<CfgType, IdMap<?>> {
	// -----------------------------------------------------------------------------------------------------------------------------
	protected <T> String idObj( CfgType type, T obj ) {
		return type.id(obj);
	}
	protected <T> T sameId( CfgType type, T fstObj, T curObj ) {
		return fstObj;
	}
	// -----------------------------------------------------------------------------------------------------------------------------
	public <T> IdMap<T> idMap( CfgType type ) {
		if(!containsKey(type) ) {
			put(type, new IdMap<T>());
		}
		return (IdMap<T>)get(type);
	}
	// -----------------------------------------------------------------------------------------------------------------------------
	public <T> void add( IdMap<T> idMap, CfgType type, String id, T obj ) {
		if( idMap.has(id) ) {
			obj = sameId(type, idMap.get(id), obj);
		}
		idMap.put(id, obj);
		if( type.hasGroup()) {
			add(type.getGroup(), id, obj);
		}
	}
	public <T> void add( CfgType type, String id, T obj ) {
		add(idMap(type), type, id, obj);
	}
	public <T> void add( CfgType type, T obj ) {
		add(type, idObj(type, obj), obj);
	}
	public <T> void add( CfgType type, List<T> lst ) {
		IdMap<T> idMap = idMap(type);
		lst.forEach(obj -> add(idMap, type, idObj(type, obj), obj));
	}
	public <U, T> void add( CfgType type, U listHost, Function<U, List<T>> listGetter ) {
		if( listHost != null ) {
			add(type, listGetter.apply(listHost));
		}
	}
	// -----------------------------------------------------------------------------------------------------------------------------
	public boolean has( CfgType type, String id ) {
		return idMap(type).has(id);
	}
	public <T> T get( CfgType type, String id ) {
		IdMap<T> idMap = idMap(type);
		return idMap.get(id);
	}
	public <T> List<T> get( CfgType type, Set<String> idSet ) {
		return (idSet != null && !idSet.isEmpty()) ? filter(type, idSet) : all(type);
	}
	public Set<String> idSet( CfgType type ) {
		return idMap(type).keySet();
	}
	public List<String> idLst( CfgType type ) {
		return new ArrayList<>(idSet(type));
	}
	public <T> List<T> all( CfgType type ) {
		return new ArrayList<T>((Collection<? extends T>)idMap(type).values());
	}
	public <T> List<T> filter( CfgType type, Set<String> idSet ) {
		return (List<T>)idMap(type).values().stream().filter(obj -> idSet.contains(idObj(type, obj))).collect(Collectors.toList());
	}
	// -----------------------------------------------------------------------------------------------------------------------------
	public String print() {
		StringBuilder sb = new StringBuilder(String.format("%nIdObjMap {%n"));
		for( CfgType type : keySet()) {
			sb.append(String.format(" %6s: %4d objects%n", type.name(), idMap(type).size()));
		}
		sb.append(String.format("}"));
		return sb.toString();
	}
	// -----------------------------------------------------------------------------------------------------------------------------
}
