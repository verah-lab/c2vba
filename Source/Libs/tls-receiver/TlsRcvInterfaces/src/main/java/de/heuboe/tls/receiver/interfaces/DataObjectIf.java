package de.heuboe.tls.receiver.interfaces;

import java.util.List;
import java.util.Map;

/*
 * A data object is something that the implementation of a transformer produces.
 * Thus it has really to be defined in the implementation context 
 * It may thought of as a complex data structure containing multiple properties.
 */
public interface DataObjectIf { // NOSONAR tag interface
        /**
         * Get the analysed contents of a de block as a list
         * @return the sequential list of properties analysed
         */
        public List<DataItem> getItems(); // don't use getItems().add()! Use addItem instead
        /**
         * 
         * Get the analysed contents of a de block as a map
         * @return A map of analaysed properties. the key ist the name of an property
         */
        public Map<String, DataItem> getItemMap();
        /**
         * Adds a property
         * @param dataItem The property to be added
         */
        public void addItem(DataItem dataItem);
        /**
         * Get the name of the DataObject / data structure
         * @return The name of the DataObeject. Mostly something like DataType.
         */
        public String getName();
        /**
         * Get Address/Id of data object
         * @return Address/Id of data object
         */
        public String getAddress();
        /**
         * Get Address/Id of data object
         * @return Address/Id of data object
         */
        default public String getId()  {
                return getAddress();
        }
        /**
         * indicates whether the data was subsequent delivered (nachgeliefert) 
         * @return Indicator whether the data was subsequent delivered (nachgeliefert)
         */
        public boolean isSubsequent();
}
