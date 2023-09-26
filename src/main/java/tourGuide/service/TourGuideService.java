package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tourGuide.constant.InternalTest;
import tourGuide.model.User;
import tourGuide.model.UserReward;
import tourGuide.tracker.Tracker;
import tripPricer.Provider;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class TourGuideService {
    private final GpsUtil gpsUtil;

    private final RewardsService rewardsService;

    public UserService userService;

    public UserRewardService userRewardService = new UserRewardService();

    private final Logger logger = LoggerFactory.getLogger(TourGuideService.class);

    public Tracker tracker;
    public InternalTest internalTest;
    boolean testMode = true;


    public TourGuideService(GpsUtil gpsUtil, GpsUtil gpsUtil1, RewardsService rewardsService, UserService userService) {
        this.gpsUtil = gpsUtil1;
        this.rewardsService = rewardsService;
        this.userService = userService;
        this.tracker = new Tracker(TourGuideService.this, userService);

        this.internalTest = new InternalTest();

        if (testMode) {
            logger.info("TestMode enabled");
            logger.debug("Initializing users");
            internalTest.initializeInternalUsers();
            logger.debug("Finished initializing users");
        }

        tracker.startTracking();

        addShutDownHook();
    }



    /**
     * Get All users
     *
     * @return a list of all user
     */
    public List<User> getAllUsers() {
        return userService.getAllUser();
    }

    public void addUser(User user) {
        userService.addUser(user);
    }

    public List<Provider> getTripDeals(User user) {
        return userRewardService.getTripDeals(user);
    }

    public List<UserReward> getUserRewards(User user) {
        return userRewardService.getUserRewards(user);
    }

    public VisitedLocation getUserLocation(User user) throws ExecutionException, InterruptedException {
        return userService.getUserLocation(user);
    }

    public CompletableFuture<VisitedLocation> trackUserLocation(User user) {
        return userService.trackUserLocation(user);
    }

    public Map<String, Location> getAllCurrentLocations() throws ExecutionException, InterruptedException {

        Map<String, Location> userLocation = new HashMap<>();
        List<User> userList = userService.getAllUser();
        if (!userList.isEmpty()) {
            for (User user : userList) {
                if (!user.getVisitedLocations().isEmpty())
                    if (user.getLastVisitedLocation().location != null)
                        userLocation.put(user.getUserId().toString(), user.getLastVisitedLocation().location);
            }
            return userLocation;
        } else {
            logger.error("TourGuideService : There no users ! ");
            return null;
        }
    }


    public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation) {

        Map<Double, Attraction> attractionMap = new HashMap<>();
        gpsUtil.getAttractions().forEach((n) -> attractionMap.put(getDistance(n, visitedLocation.location), n));

        TreeMap<Double, Attraction> sortedAttraction = new TreeMap<>(attractionMap);
        return new ArrayList<>(sortedAttraction.values()).subList(0, 5);

    }


    /**
     * Create an ExecutorService thread pool in which runnable of trackUserLocation is executed
     *
     * @param userList the list containing all users
     */
    public void trackListUserLocation(List<User> userList) throws InterruptedException {
        userService.trackListUserLocation(userList);
    }

    public double getDistance(Location locationA, Location locationB) {
        return Math.sqrt((locationB.longitude - locationA.longitude) * (locationB.longitude - locationA.longitude) + (locationB.latitude - locationA.latitude) * (locationB.latitude - locationA.latitude));

    }

    private void addShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(tracker::stopTracking));
    }




}
