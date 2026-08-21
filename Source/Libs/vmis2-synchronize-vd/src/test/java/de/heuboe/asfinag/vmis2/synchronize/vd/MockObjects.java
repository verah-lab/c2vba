package de.heuboe.asfinag.vmis2.synchronize.vd;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import de.heuboe.asfinag.vmis2.infrastructure.base.GeoReference;
import de.heuboe.asfinag.vmis2.infrastructure.base.InfrastructureObject;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.AbstractData;
import eu.vmis_ehe.vmis2.control.data.pojo.PInfrastructureObject;
import eu.vmis_ehe.vmis2.control.data.pojo.PValiditySection;
import eu.vmis_ehe.vmis2.geomanager.features.PointRoadReference;

public class MockObjects {

    public static class LogKm implements GeoReference {
        private String roadId;
        private double kmFrom; // in km
        private double kmTo; // in km

        public LogKm(String roadId, double kmFrom, double kmTo) {
            this.roadId = roadId;
            this.kmFrom = kmFrom;
            this.kmTo = kmTo;
        }

        @Override
        public String getRoadId() {
            return roadId;
        }

        @Override
        public double getKmFrom() {
            return kmFrom;
        }

        @Override
        public double getKmTo() {
            return kmTo;
        }

        @Override
        public PValiditySection getValiditySection() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Optional<PointRoadReference> getPointRoadReference() {
            // TODO Auto-generated method stub
            return null;
        }
    }

    /**
     * MockObject to indicate the infrastructure object "lane".
     * The id has to be same id as in ShortTermRecordingLaneData(protobuf), which corresponds to the old 'eaid' from 'DaLVEKurzErfassung' (derTest).
     */
    public static class Lane implements InfrastructureObject {

        private String id;
        private String name;
        private String shortName;
        private String version;
        private GeoReference geoReference;


        /**
         * Constructs a lane infrastructure object.
         *
         * @param id    The id has to be same id as in ShortTermRecordingLaneData(protobuf),
         *              which corresponds to the old 'eaid' from 'DaLVEKurzErfassung'  (derTest).
         * @param name  The name of the lane.
         * @param shortName The short name of the lane.
         * @param version The version
         * @param geoReference references the geographic location of the lane.
         */
        public Lane(String id, String name, String shortName, String version, GeoReference geoReference) {
            this.id = id;
            this.name = name;
            this.shortName = shortName;
            this.version = version;
            this.geoReference = geoReference;
        }

        public String getId() {
            return id;
        }


        public String getType() {
            return "Lane";
        }


        public Optional<String> getName() {
            return Optional.ofNullable(name);
        }


        public Optional<String> getShortName() {
            return Optional.ofNullable(shortName);
        }


        public Optional<String> getVersion() {
            return Optional.ofNullable(version);
        }


        public Optional<GeoReference> getGeoReference() {
            return Optional.ofNullable(geoReference);
        }

        public List<InfrastructureObject> getReferences(String type) {
            return Collections.emptyList();
        }

        @Override
        public Map<String, List<InfrastructureObject>> getReferences() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <T> Optional<T> getAttachedData(Class<T> clazz) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void updateReferences(String refType, List<InfrastructureObject> references) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public Map<String, Object> getAttachedData() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void updateAttachedData(Object obj) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void removeAttachedData(Object obj) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void setAttachedDataMap(Map<String, Object> attachedDataMap) {
            // TODO Auto-generated method stub
            
        }

		@Override
		public PInfrastructureObject getInfrastructureObject() {
			// TODO Auto-generated method stub
			return null;
		}
    }

    public static class TestAbstractData extends AbstractData {
        //another implementation of AbstractData - only for test purposes
    }
}