package de.heuboe.vmis2.jprotoc.transferinterface;

import com.google.protobuf.ProtocolMessageEnum;

/**
 * Marker interface for all java enum that have been generated from or for
 * {@link ProtocolMessageEnum protobuf message enums}.
 *
 * <p>
 * All implementing classes (including subclasses) must have the following public, static,
 * threadsafe and stateless methods:
 * </p>
 *
 * <ul>
 * <li><code>from(Protobuf) : Enum</code></li>
 * <li><code>to(Enum) : Protobuf</code></li>
 * <li><code>forNumber(int) : Enum</code></li>
 * <li><code>transfer() : BaseTransfer&lt;Protobuf, Enum&gt;</code></li>
 * </ul>
 */
public interface HbProtoBufJavaEnum extends HbProtoBufJavaBase {

    /**
     * Gets the number of this instance as defined by its equivalent in the proto files.
     *
     * @return The number assigned to this instance.
     */
    int getNumber();

}
