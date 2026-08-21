package de.heuboe.tls.receiver.interfaces.helpers;

/**
 * Use this class to keep the elements identifying a tls datatype together
 * @author ronald
 *
 */
public class TlsDatatypeId implements Comparable<TlsDatatypeId> {
        private short fg;
        private short id;
        private short typ;
        private short job; // optional, future use

        /**
         * Constructor
         * @param fg Funktionsgruppe
         * @param id Identifier
         * @param typ Typ (DE-Block-Typ)
         */
        public TlsDatatypeId( short fg, short id, short typ ) {
                super();
                this.fg = fg;
                this.id = id;
                this.typ = typ;
                this.job = 0;
        }
        
        /**
         * Constructor
         * @param fg Funktionsgruppe
         * @param id Identifier
         * @param typ Typ (DE-Block-Typ)
         * @param job Jobnummer, currently not relevant
         */
        public TlsDatatypeId( short fg, short id, short typ, short job ) {
                super();
                this.fg = fg;
                this.id = id;
                this.typ = typ;
                this.job = job;
        }

        public short getFg() {
                return fg;
        }

        public short getId() {
                return id;
        }

        public short getTyp() {
                return typ;
        }

        public short getJob() {
                return job;
        }

        public int compareTo( TlsDatatypeId other ) {
                if ( this == other ) {
                        return 0;
                }
                if ( other == null ) {
                        throw new NullPointerException( "other: compareTo( TlsDatatypeId other )" );
                }
                if ( fg < other.fg ) {
                        return -1;
                }
                if ( fg > other.fg ) {
                        return 1;
                }
                if ( id < other.id ) {
                        return -1;
                }
                if ( id > other.id ) {
                        return 1;
                }
                if ( typ < other.typ ) {
                        return -1;
                }
                if ( typ > other.typ ) {
                        return 1;
                }
                if ( job < other.job ) {
                        return -1;
                }
                if ( job > other.job ) {
                        return 1;
                }
                return 0;
        }

        @Override
        public int hashCode() {
                final int prime = 3559;
                int result = 1;
                result = prime * result + fg;
                result = prime * result + id;
                result = prime * result + typ;
                result = prime * result + job;
                return result;
        }

        @Override
        public boolean equals( Object obj ) {
                if ( this == obj )
                        return true;
                if ( obj == null )
                        return false;
                if ( getClass() != obj.getClass() )
                        return false;
                TlsDatatypeId other = (TlsDatatypeId) obj;
                if ( fg != other.fg ) {
                        return false;
                }
                if ( id != other.id ) {
                        return false;
                }
                if ( typ != other.typ ) {
                        return false;
                }
                return ( job == other.job );
        }

        @Override
        public String toString() {
                return "TlsDatatypeId [fg=" + fg + ", id=" + id + ", typ=" + typ + ", job=" + job + "]";
        }
        
}
