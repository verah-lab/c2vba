package de.heuboe.tls.receiver.getter;

public abstract class AbstractGetterWithSizeCol extends AbstractGetter {
        private String sizeCol;

        protected AbstractGetterWithSizeCol( String name ) {
               super( name );
               sizeCol = null;
        }

        public String getSizeCol() {
                return sizeCol;
        }

        public void setSizeCol( String sizeCol ) {
                this.sizeCol = sizeCol;
        }

}
