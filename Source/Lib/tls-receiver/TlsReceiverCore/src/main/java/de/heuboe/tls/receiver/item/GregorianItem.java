package de.heuboe.tls.receiver.item;

import java.util.Date;
import java.util.GregorianCalendar;

import de.heuboe.tls.receiver.interfaces.DataItem;

public class GregorianItem extends AbstractDataItem {

        private GregorianCalendar value;

        public GregorianItem(String name, GregorianCalendar value, int size) {
                super(name, size);
                this.value = value;
        }
        
        @Override
        public DataItemType getType() {
                return DataItemType.GREGORIAN;
        }

        @Override
        public Long getAsLong() {
                return value.getTimeInMillis();
        }

        @Override
        public Date getAsDate() {
                return value.getTime();
        }

        @Override
        public GregorianCalendar getAsGregorianCalendar() {
                return value;
        }

        @Override
        public DataItem copy() {
                return new GregorianItem(name, value, consumedSize);
        }

}
