package de.heuboe.vmis2.jprotoc.transferinterface;

import com.google.protobuf.GeneratedMessageV3;

/**
 * Marker interface for pojo classes that have been generated from or for {@link GeneratedMessageV3
 * ProtoBuf Messages}.
 *
 * <p>
 * All implementing classes (including subclasses) must have the following public, static,
 * threadsafe and stateless methods:
 * </p>
 *
 * <ul>
 * <li><code>from(Protobuf) : Pojo</code></li>
 * <li><code>to(Pojo) : Protobuf</code></li>
 * <li><code>fromBytes(byte[]) : Pojo</code></li>
 * <li><code>toBytes(Pojo) : byte[]</code></li>
 * <li><code>transfer() : PojoTransfer&lt;Protobuf, Pojo&gt;</code></li>
 * </ul>
 */
public interface HbProtoBufPojo extends HbProtoBufJavaBase { // marker only
}
