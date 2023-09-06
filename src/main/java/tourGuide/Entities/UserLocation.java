package tourGuide.Entities;

import gpsUtil.GpsUtil;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tourGuide.model.User;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class UserLocation {

    private final RewardsService rewardsService;
    private Logger log = LoggerFactory.getLogger(TourGuideService.class);

    private final GpsUtil gpsUtil;


    public UserLocation(RewardsService rewardsService) {
        this.rewardsService = rewardsService;
    }

    public VisitedLocation getUserLocation(User user) throws ExecutionException, InterruptedException {
        return (user.getVisitedLocations().isEmpty()) ?
                trackUserLocation(user).get() : user.getLastVisitedLocation();
    }

    public CompletableFuture<VisitedLocation> trackUserLocation(User user) {
        log.debug("Tracking user " + user.getUsername());

        return CompletableFuture.supplyAsync(() -> {
            VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
            user.addToVisitedLocations(visitedLocation);
            rewardsService.calculateRewards(user);
            return visitedLocation;
        }, executorService);
    }
}
