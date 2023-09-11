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
import java.util.UUID;

@Service
public class RewardsService {
	private int proximityBuffer;
	private final GpsUtil gpsUtil;
	private final RewardCentral rewardsCentral;
	private final UserService userService;
	private Logger logger = LoggerFactory.getLogger(TourGuideService.class);


	public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral, UserService userService) {
		this.gpsUtil = gpsUtil;
		this.rewardsCentral = rewardCentral;
		this.userService = userService;
	}


	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}


	public void setDefaultProximityBuffer() {
		proximityBuffer = TourGuideConstant.DEFAULT_PROXIMITY_BUFFER;
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

	//Check if a VisitedLocation is within range of an Attraction
	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return !(getDistance(attraction, visitedLocation.location) > proximityBuffer);
	}

	//Get reward point value of an attraction for a provided UUID
	private int getRewardPoints(Attraction attraction, UUID userid) {

		return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, userid);
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

	/**
	 * Calculate rewards for a provided User
	 * <p>
	 * Checks all user's VisitedLocations, compares each to list of Attractions from GpsService
	 * If a user has visited an attraction (ie visited location is in range of an attraction)
	 * Add reward to user for that attraction if they have not already received a reward for it
	 *
	 * @param user User object
	 */
	public void calculateRewards(User user) {
		List<VisitedLocation> userLocations = user.getVisitedLocations();
		List<Attraction> attractions = gpsUtil.getAttractions();

		for(VisitedLocation visitedLocation : userLocations) {
			for(Attraction attraction : attractions) {
				if(user.getUserRewards().stream().filter(r -> r.attraction.attractionName.equals(attraction.attractionName)).count() == 0) {
					if(nearAttraction(visitedLocation, attraction)) {
						user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user.getUserId())));
					}
				}
			}
		}
	}



}
