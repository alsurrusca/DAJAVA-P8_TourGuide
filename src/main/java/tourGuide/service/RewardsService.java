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

@Service
public class RewardsService {
	private int proximityBuffer;
	private final GpsUtil gpsUtil;
	private final RewardCentral rewardsCentral;

	private Logger logger = LoggerFactory.getLogger(TourGuideService.class);


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
		List<VisitedLocation> userLocations = user.getVisitedLocations();
		List<Attraction> attractions = gpsUtil.getAttractions();

		for (VisitedLocation visitedLocation : userLocations) {
			for (Attraction attraction : attractions) {
				if (nearAttraction(visitedLocation, attraction)) {
					logger.debug("Add a reward for " + user.getUsername() + "for attraction " + attraction.attractionName);
					user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
				}
			}
		}
	}


	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		double distance = getDistance(attraction, visitedLocation.location);
		return distance <= proximityBuffer;
	}

	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		double distance = getDistance(attraction, location);
		return distance <= TourGuideConstant.ATTRACTION_PROXIMITY_RANGE;
	}

	public int getRewardPoints(Attraction attraction, User user) {
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
		double statuteMiles = TourGuideConstant.STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
		return statuteMiles;
	}
}
