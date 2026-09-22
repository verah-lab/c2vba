package de.heuboe.asfinag.vmis2.infrastructure.types;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Object class with details to attach on lane infrastructure objects.
 */
@Slf4j
@Data
@AllArgsConstructor
public class LaneDetails {
    public static final String LANE_NEIGHBOUR_REFERENCE_TYPE = "LANE_NEIGHBOUR";

    /**
     * Logical Passivation if lane is passivated
     */
    private boolean isPassivated;

    /**
     * Index for identifying the lane. Normally identical to GlobalH-Index, but will be differ
     * when a lane is passivated.
     */
    private int laneIndex;

    /**
     * Index for identifying the lane.
     */
    private int globalHIndex;


    /**
     * Constructor of passivated lane.
     */
    public LaneDetails() {
        this.isPassivated = false;
        laneIndex = -1;
        globalHIndex = -1;
    }

    /**
     * Constructor without laneIndex. LaneIndex remains -1.
     * @param isPassivated true if lane is logical passivated
     * @param globalHIndex index of lane
     */
    public LaneDetails(boolean isPassivated, int globalHIndex) {
        this.isPassivated = isPassivated;
        this.globalHIndex = globalHIndex;
        this.laneIndex = -1;
    }

}
