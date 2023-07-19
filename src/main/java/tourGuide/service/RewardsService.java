package tourGuide.service;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tourGuide.model.User;

/**
 * Rewards Calculation
 */

public interface RewardsService {

    /**
     * Max distance between location and attraction
     */

     void setProximityBuffer(int proximityBuffer);

    /**
     * Max distance of two location. If they are close enough each other.
     */

     void setDefaultProximityBuffer();

    /**
     * Calculate Rewards for each attraction who are visited.
     */
     void calculateRewards(User user);

    /**
     * True -> user's location proximity with attraction's location
     *
     * @param attraction location
     * @param location   visited
     * @return true if proximity less than proximity buffer
     */
     boolean isWithinAttractionProximity(Attraction attraction, Location location);

    /**
     *
     * @param attraction -> visited by user
     * @param user
     * @return number of rewards
     */
     int getRewardPoints(Attraction attraction, User user);

    /**
     * Distance between 2 locations in miles
     * @param loc1
     * @param loc2
     * @return distance in miles
     */
     double getDistance(Location loc1, Location loc2);
}