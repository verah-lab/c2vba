package de.heuboe.tls.receiver.rdr.getter;

public abstract class AbstractGetterWithSizeCol extends AbstractGetter {
        private String sizeCol;
        private String targetType = ""; // the type of data the analysed value should be converted to.

        protected AbstractGetterWithSizeCol( String name, String targetType ) {
               super( name );
               sizeCol = null;
               this.targetType = targetType;
        }

        public String getSizeCol() {
                return sizeCol;
        }

        public void setSizeCol( String sizeCol ) {
                this.sizeCol = sizeCol;
        }

        @Override
        public String getTargetType() {
                return this.targetType;
        }

}
