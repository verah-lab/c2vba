// Generated from file Test.proto as of 2020-05-05 11:20:41 +0200 (MESZ)
// by de.heuboe.asfinag / vmis2-jprotoc-plugin / 4.0.4-SNAPSHOT built 2020-05-05 10:35:31 +0200 (MESZ)

package de.heuboe.vmis2.jprotoc.test.pojo;

import java.io.IOException;

import org.springframework.data.annotation.PersistenceConstructor;

import com.google.protobuf.InvalidProtocolBufferException;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Value;


/**
 * Protopojo type {@link PTestMessage PTestMessage (Pojo)}
 * for protobuf type {@link de.heuboe.vmis2.jprotoc.test.TestMessage TestMessage (Proto)}.
 *
 * <p> An example message.</p>
 *
 */
@Value
@Builder(toBuilder=true, builderClassName="PTestMessageBuilder")
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@ToString(doNotUseGetters = true) //don't use getter to not trigger lazy PAny initialization
public class PTestMessage implements de.heuboe.vmis2.jprotoc.transferinterface.HbProtoBufPojo {

    /**
     * Protopojo type {@link InnerMessage InnerMessage (Pojo)}
     * for protobuf type {@link de.heuboe.vmis2.jprotoc.test.TestMessage.InnerMessage InnerMessage (Proto)}.
     *
     * <p> An example inner message.</p>
     *
     */
    @Value
    @Builder(toBuilder=true, builderClassName="InnerMessageBuilder")
    @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
    @ToString(doNotUseGetters = true) //don't use getter to not trigger lazy PAny initialization
    public static class InnerMessage implements de.heuboe.vmis2.jprotoc.transferinterface.HbProtoBufPojo {


        String value;

        /**
         * All Args Constructor (without Any)
         *
         * @param value see getter 
         */
        @PersistenceConstructor
        public InnerMessage(
          String value
        ){
            this.value = value;
        }




        /**
         * Converts the given protobuf instance to an instance of this pojo class.
         *
         * @param proto The protobuf instance to convert.
         * @return The converted pojo instance.
         */
        public static InnerMessage from(de.heuboe.vmis2.jprotoc.test.TestMessage.InnerMessage proto) {
            InnerMessageBuilder builder = InnerMessage.builder();
            {
                builder.value(proto.getValue());
            }
            return builder.build();
        }

        /**
         * Converts the given pojo to an instance of the related protobuf class.
         *
         * @param pojo The pojo to convert.
         * @return The converted protobuf instance.
         */
        public static de.heuboe.vmis2.jprotoc.test.TestMessage.InnerMessage to(InnerMessage pojo) {

            de.heuboe.vmis2.jprotoc.test.TestMessage.InnerMessage.Builder builder = de.heuboe.vmis2.jprotoc.test.TestMessage.InnerMessage.newBuilder();
            {
                String var = pojo.getValue();
                if (null != var) {
                    builder.setValue(
                        var
                    );
                }
            }
            return builder.build();
        }

        /**
         * Serializes the given pojo to a byte array.
         *
         * @param pojo The pojo to convert.
         * @return The byte array representing the given pojo.
         */
        public static byte[] toBytes(InnerMessage pojo) {
            return to(pojo).toByteArray();
        }

        /**
         * Deserializes the given input bytes to a pojo class.
         *
         * @param input The input bytes to convert.
         * @return The pojo represented by the given byte array.
         * @throws InvalidProtocolBufferException If the given input data do not match those required for a
         *         pojo.
         */
        public static InnerMessage fromBytes( byte[] input ) throws InvalidProtocolBufferException {
            try {
                return from( de.heuboe.vmis2.jprotoc.test.TestMessage.InnerMessage.parseFrom(input) );
            } catch (IllegalArgumentException|NullPointerException e) {
                throw new InvalidProtocolBufferException("When converting pojo from byte[]", new IOException(e));
            }
        }

        /**
         * Returns the transfer instance that provides methods for easy conversion from and to pojo and
         * protobuf instances or plain bytes.
         *
         * @return The transfer instance.
         */
        public static Transfer transfer() {
            return Transfer.INSTANCE;
        }

        /**
         * The transfer class belonging to the {@link InnerMessage}.
         */
        public static final class Transfer implements de.heuboe.vmis2.jprotoc.transferinterface.PojoTransfer<de.heuboe.vmis2.jprotoc.test.TestMessage.InnerMessage, InnerMessage> {

            /**
             * The singleton instance of this transfer class.
             */
            public static final Transfer INSTANCE = new Transfer();

            private Transfer() {}

            @Override
            public Class<de.heuboe.vmis2.jprotoc.test.TestMessage.InnerMessage> protoClass() {
                return de.heuboe.vmis2.jprotoc.test.TestMessage.InnerMessage.class;
            }

            @Override
            public Class<InnerMessage> pojoClass() {
                return InnerMessage.class;
            }

            @Override
            public com.google.protobuf.Descriptors.Descriptor getDescriptor() {
                return de.heuboe.vmis2.jprotoc.test.TestMessage.InnerMessage.getDescriptor();
            }

            @Override
            public InnerMessage fromProto(de.heuboe.vmis2.jprotoc.test.TestMessage.InnerMessage proto) {
                return InnerMessage.from(proto);
            }

            @Override
            public de.heuboe.vmis2.jprotoc.test.TestMessage.InnerMessage toProto(InnerMessage pojo) {
                return  InnerMessage.to(pojo);
            }

            @Override
            public byte[] toBytes(InnerMessage pojo) {
                return to(pojo).toByteArray();
            }

            @Override
            public InnerMessage fromBytes(byte[] input) throws InvalidProtocolBufferException {
                return from(protoFromBytes(input));
            }

            @Override
            public de.heuboe.vmis2.jprotoc.test.TestMessage.InnerMessage protoFromBytes(byte[] input) throws InvalidProtocolBufferException {
                return de.heuboe.vmis2.jprotoc.test.TestMessage.InnerMessage.parseFrom(input);
            }

            @Override
            public int hashCode() {
                return getClass().getName().hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof InnerMessage.Transfer;
            }

            @Override
            public String toString() {
                return getClass().getName() + " (Pojo)";
            }

        }

    }

    String value;

    /**
     * All Args Constructor (without Any)
     *
     * @param value see getter 
     */
    @PersistenceConstructor
    public PTestMessage(
      String value
    ){
        this.value = value;
    }




    /**
     * Converts the given protobuf instance to an instance of this pojo class.
     *
     * @param proto The protobuf instance to convert.
     * @return The converted pojo instance.
     */
    public static PTestMessage from(de.heuboe.vmis2.jprotoc.test.TestMessage proto) {
        PTestMessageBuilder builder = PTestMessage.builder();
        {
            builder.value(proto.getValue());
        }
        return builder.build();
    }

    /**
     * Converts the given pojo to an instance of the related protobuf class.
     *
     * @param pojo The pojo to convert.
     * @return The converted protobuf instance.
     */
    public static de.heuboe.vmis2.jprotoc.test.TestMessage to(PTestMessage pojo) {

        de.heuboe.vmis2.jprotoc.test.TestMessage.Builder builder = de.heuboe.vmis2.jprotoc.test.TestMessage.newBuilder();
        {
            String var = pojo.getValue();
            if (null != var) {
                builder.setValue(
                    var
                );
            }
        }
        return builder.build();
    }

    /**
     * Serializes the given pojo to a byte array.
     *
     * @param pojo The pojo to convert.
     * @return The byte array representing the given pojo.
     */
    public static byte[] toBytes(PTestMessage pojo) {
        return to(pojo).toByteArray();
    }

    /**
     * Deserializes the given input bytes to a pojo class.
     *
     * @param input The input bytes to convert.
     * @return The pojo represented by the given byte array.
     * @throws InvalidProtocolBufferException If the given input data do not match those required for a
     *         pojo.
     */
    public static PTestMessage fromBytes( byte[] input ) throws InvalidProtocolBufferException {
        try {
            return from( de.heuboe.vmis2.jprotoc.test.TestMessage.parseFrom(input) );
        } catch (IllegalArgumentException|NullPointerException e) {
            throw new InvalidProtocolBufferException("When converting pojo from byte[]", new IOException(e));
        }
    }

    /**
     * Returns the transfer instance that provides methods for easy conversion from and to pojo and
     * protobuf instances or plain bytes.
     *
     * @return The transfer instance.
     */
    public static Transfer transfer() {
        return Transfer.INSTANCE;
    }

    /**
     * The transfer class belonging to the {@link PTestMessage}.
     */
    public static final class Transfer implements de.heuboe.vmis2.jprotoc.transferinterface.PojoTransfer<de.heuboe.vmis2.jprotoc.test.TestMessage, PTestMessage> {

        /**
         * The singleton instance of this transfer class.
         */
        public static final Transfer INSTANCE = new Transfer();

        private Transfer() {}

        @Override
        public Class<de.heuboe.vmis2.jprotoc.test.TestMessage> protoClass() {
            return de.heuboe.vmis2.jprotoc.test.TestMessage.class;
        }

        @Override
        public Class<PTestMessage> pojoClass() {
            return PTestMessage.class;
        }

        @Override
        public com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return de.heuboe.vmis2.jprotoc.test.TestMessage.getDescriptor();
        }

        @Override
        public PTestMessage fromProto(de.heuboe.vmis2.jprotoc.test.TestMessage proto) {
            return PTestMessage.from(proto);
        }

        @Override
        public de.heuboe.vmis2.jprotoc.test.TestMessage toProto(PTestMessage pojo) {
            return  PTestMessage.to(pojo);
        }

        @Override
        public byte[] toBytes(PTestMessage pojo) {
            return to(pojo).toByteArray();
        }

        @Override
        public PTestMessage fromBytes(byte[] input) throws InvalidProtocolBufferException {
            return from(protoFromBytes(input));
        }

        @Override
        public de.heuboe.vmis2.jprotoc.test.TestMessage protoFromBytes(byte[] input) throws InvalidProtocolBufferException {
            return de.heuboe.vmis2.jprotoc.test.TestMessage.parseFrom(input);
        }

        @Override
        public int hashCode() {
            return getClass().getName().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof PTestMessage.Transfer;
        }

        @Override
        public String toString() {
            return getClass().getName() + " (Pojo)";
        }

    }

}
