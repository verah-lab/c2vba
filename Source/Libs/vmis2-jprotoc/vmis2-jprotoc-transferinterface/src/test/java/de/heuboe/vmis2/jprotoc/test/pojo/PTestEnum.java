// Generated from file Test.proto as of 2020-05-05 11:20:41 +0200 (MESZ)
// by de.heuboe.asfinag / vmis2-jprotoc-plugin / 4.0.4-SNAPSHOT built 2020-05-05 10:35:31 +0200 (MESZ)

package de.heuboe.vmis2.jprotoc.test.pojo;

/**
 * Protopojo type {@link PTestEnum PTestEnum (Pojo)}
 * for protobuf type {@link de.heuboe.vmis2.jprotoc.test.TestEnum TestEnum (Proto)}.
 *
 * <p> An example enum.</p>
 *
 */
public enum PTestEnum implements de.heuboe.vmis2.jprotoc.transferinterface.HbProtoBufJavaEnum {
    A(0),
    B(1),
    C(2),
    D(3),
    UNRECOGNIZED(-1);

    private int value;

    private PTestEnum(int value) {
        this.value = value;
    }

    /**
     * Gets the enums's numeric value as defined in the .proto file.
     *
     * @return The enum's numeric value.
     */
    @Override
    public final int getNumber() {
        if (this == UNRECOGNIZED) {
            throw new java.lang.IllegalArgumentException(
                "Can't get the number of an unknown enum value.");
        }
        return value;
    }

    /**
     * Converts the given proto enum value to its corresponding pojo instance.
     *
     * @param proto The proto enum value to convert.
     * @return The pojo enum value.
     */
    public static PTestEnum from(de.heuboe.vmis2.jprotoc.test.TestEnum proto) {
        if (de.heuboe.vmis2.jprotoc.test.TestEnum.UNRECOGNIZED == proto) {
            return UNRECOGNIZED;
        }
        return forNumber(proto.getNumber());
    }

    /**
     * Converts the given pojo enum value to its corresponding proto instance.
     *
     * @param pojo The pojo enum value to convert.
     * @return The proto enum value.
     */
    public static de.heuboe.vmis2.jprotoc.test.TestEnum to(PTestEnum pojo) {
        if (UNRECOGNIZED == pojo) {
            return de.heuboe.vmis2.jprotoc.test.TestEnum.UNRECOGNIZED;
        }
        return de.heuboe.vmis2.jprotoc.test.TestEnum.forNumber(pojo.getNumber());
    }

    /**
     * Returns the enum instance belonging to the given number based on the definition in the .proto
     * file.
     *
     * @param value The number of the enum to search.
     * @return The enum instance belonging to the number. Returns the 0-value enum 
     *         if the number does not belong to any enum instance.
     */
    public static PTestEnum forNumber(int value) {
        switch (value) {
            default: // Return default value
            case 0: return A;
            case 1: return B;
            case 2: return C;
            case 3: return D;
        }
    }

    /**
     * Returns the transfer instance that provides methods for easy conversion from and to pojo and
     * protobuf instances.
     *
     * @return The transfer instance.
     */
    public static Transfer transfer() {
        return Transfer.INSTANCE;
    }

    /**
     * The transfer class belonging to the {@link PTestEnum}.
     */
    public static final class Transfer implements de.heuboe.vmis2.jprotoc.transferinterface.EnumTransfer<de.heuboe.vmis2.jprotoc.test.TestEnum, PTestEnum> {

        /**
         * The singleton instance of this transfer class.
         */
        public static final Transfer INSTANCE = new Transfer();

        private Transfer() {}

        @Override
        public Class<de.heuboe.vmis2.jprotoc.test.TestEnum> protoClass() {
            return de.heuboe.vmis2.jprotoc.test.TestEnum.class;
        }

        @Override
        public Class<PTestEnum> pojoClass() {
            return PTestEnum.class;
        }

        @Override
        public com.google.protobuf.Descriptors.EnumDescriptor getDescriptor() {
            return de.heuboe.vmis2.jprotoc.test.TestEnum.getDescriptor();
        }

        @Override
        public PTestEnum fromProto(de.heuboe.vmis2.jprotoc.test.TestEnum proto) {
            return PTestEnum.from(proto);
        }

        @Override
        public de.heuboe.vmis2.jprotoc.test.TestEnum toProto(PTestEnum pojo) {
            return PTestEnum.to(pojo);
        }

        @Override
        public PTestEnum fromNumber(int number) {
            return PTestEnum.forNumber(number);
        }

        @Override
        public de.heuboe.vmis2.jprotoc.test.TestEnum protoFromNumber(int number) {
            return de.heuboe.vmis2.jprotoc.test.TestEnum.forNumber(number);
        }

        @Override
        public int hashCode() {
            return getClass().getName().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof PTestEnum.Transfer;
        }

        @Override
        public String toString() {
            return getClass().getName() + " (Enum)";
        }

    }

}
