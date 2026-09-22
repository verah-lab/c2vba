package de.heuboe.vmis2.jprotoc.transferinterface;

/**
 * Marker interface for all java classes that have been generated from or for protobuf messages.
 *
 * <p>
 * All implementing classes (including subclasses) must have the following public, static,
 * threadsafe and stateless methods:
 * </p>
 *
 * <ul>
 * <li><code>from(Protobuf) : Pojo</code></li>
 * <li><code>to(Pojo) : Protobuf</code></li>
 * <li><code>transfer() : BaseTransfer&lt;Protobuf, Pojo&gt;</code></li>
 * </ul>
 */
public interface HbProtoBufJavaBase { // marker only
}
