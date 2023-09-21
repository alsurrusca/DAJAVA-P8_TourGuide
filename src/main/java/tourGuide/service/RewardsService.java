package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;
import tourGuide.constant.TourGuideConstant;
import tourGuide.model.User;
import tourGuide.model.UserReward;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class RewardsService {
    private int proximityBuffer;
    private final GpsUtil gpsUtil;
    private final RewardCentral rewardsCentral;

    private final Logger logger = LoggerFactory.getLogger(TourGuideService.class);


    public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
        this.gpsUtil = gpsUtil;
        this.rewardsCentral = rewardCentral;
    }

    public void setProximityBuffer(int proximityBuffer) {
        this.proximityBuffer = proximityBuffer;
    }


    public void setDefaultProximityBuffer() {
        proximityBuffer = TourGuideConstant.DEFAULT_PROXIMITY_BUFFER;
    }

    public void calculateRewards(User user) {

        ExecutorService executorService = Executors.newCachedThreadPool();
        CompletableFuture.runAsync(() -> {
                    List<VisitedLocation> userLocations = user.getVisitedLocations();
                    List<Attraction> attractions = gpsUtil.getAttractions();

                    getRewards(user, userLocations, attractions);
                }, executorService)
                .handle((res, ex) -> {
                    if (ex != null) {
                        logger.error("RewardsService : Something wrong  " + ex.getMessage());
                    }
                    return res;
                });
        executorService.shutdownNow();
    }

    /**
     * Calculates and assign rewards to a user based on their visited locations and attractions
     *
     * @param user - user
     * @param userLocations - user's location
     * @param attractions - list of attractions
     */
    void getRewards(User user, List<VisitedLocation> userLocations, List<Attraction> attractions) {
        //Iterate through each visited location of the user
        for (VisitedLocation visitedLocation : userLocations) {
            // Each attraction
            for (Attraction attraction : attractions) {
                // Check if the attractions is new for user
                if (isNewReward(user, attraction)) {
                    //Check if the user is near the attraction
                    if (nearAttraction(visitedLocation, attraction)) {
                        //Calculate reward points and a new reward for the user
                        user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
                    }
                }
            }
        }
    }

    /**
     * Check if the attraction is new for the user
     *
     * @param user - user
     * @param attraction - attraction
     * @return - stream check if no existing reward has the same attraction name
     */
    boolean isNewReward(User user, Attraction attraction) {
        return user.getUserRewards()
                .stream()
                .noneMatch(r -> r.attraction.attractionName.equals(attraction.attractionName));
    }


    /**
     * Check if a provided Location is within range of an Attraction
     *
     * @param attraction attraction to be checked
     * @param location   location to be compared to
     * @return true if location is within range, otherwise false
     */
    public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
        return !(getDistance(attraction, location) > TourGuideConstant.ATTRACTION_PROXIMITY_RANGE);
    }

    /**
     * Check if a given attraction is near a visited location
     *
     * @param visitedLocation - visite
     * @param attraction - attraction
     * @return True if the attraction is near the visited location
     */
    private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
        return !(getDistance(attraction, visitedLocation.location) > proximityBuffer);
    }

    //Get reward point value of an attraction for a provided UUID
    private int getRewardPoints(Attraction attraction, User user) {

        return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
    }

    public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        return TourGuideConstant.STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
    }


}
