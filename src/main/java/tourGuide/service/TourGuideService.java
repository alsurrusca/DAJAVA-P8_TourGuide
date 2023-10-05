package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.User;
import tourGuide.model.UserReward;
import tourGuide.tracker.Tracker;
import tripPricer.Provider;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

@Service
public class TourGuideService {
    private GpsUtil gpsUtil = new GpsUtil();

    private RewardsService rewardsService = new RewardsService(gpsUtil,new RewardCentral());

    public UserService userService = new UserService();

    public UserRewardService userRewardService = new UserRewardService();

    private final Logger logger = LoggerFactory.getLogger(TourGuideService.class);

    public Tracker tracker;
    boolean testMode = true;
    private final int threadPoolSize = 500;

    private final ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);



    public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService, UserService userService) {
        this.gpsUtil = gpsUtil;
        this.rewardsService = rewardsService;
        this.userService = userService;


        Locale.setDefault(Locale.US);

        if (testMode) {
            logger.info("TestMode enabled");
            logger.debug("Initializing users");
            initializeInternalUsers();
            logger.debug("Finished initializing users");
        }

        tracker = new Tracker(this);

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

    /**
     * Get All currentLocations
     */
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

    /**
     * Get nearby Location of user
     */
    public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation) {

        Map<Double, Attraction> attractionMap = new HashMap<>();
        gpsUtil.getAttractions().forEach((n) -> attractionMap.put(getDistance(n, visitedLocation.location), n));

        TreeMap<Double, Attraction> sortedAttraction = new TreeMap<>(attractionMap);
        return new ArrayList<>(sortedAttraction.values()).subList(0, 5);

    }
    /**
     * Track all users' current location
     */
    public void trackAllUserLocations() {
        List<User> allUsers = userService.getAllUser();

        ArrayList<CompletableFuture> futures = new ArrayList<>();

        logger.debug("trackAllUserLocations: Creating futures for " + allUsers.size() + " user(s)");
        allUsers.forEach((n)-> {
            futures.add(
                    CompletableFuture.supplyAsync(()-> {
                        return userService.addToVisitedLocations(gpsUtil.getUserLocation(n.getUserId()), n.getUsername());
                    }, executorService)
            );
        });
        logger.debug("trackAllUserLocations: Futures created: " + futures.size() + ". Getting futures...");
        futures.forEach((n)-> {
            try {
                n.get();
            } catch (InterruptedException | ExecutionException e) {
                logger.error(String.valueOf(e));
            }
        });
        logger.debug("trackAllUserLocations: Done!");

    }

    /**
     * Track all users' current location and check if there is new rewards
     */
    public void trackAllUserLocationsAndProcess() {


        List<User> allUsers = userService.getAllUser();

        ArrayList<CompletableFuture> futures = new ArrayList<>();

        logger.debug("trackAllUserLocationsAndProcess: Creating futures for " + allUsers.size() + " user(s)");
        allUsers.forEach((n)-> {
            futures.add(
                    CompletableFuture.supplyAsync(()-> {
                                return userService.addToVisitedLocations(gpsUtil.getUserLocation(n.getUserId()), n.getUsername());
                            }, executorService)
                            .thenAccept(y -> {rewardsService.calculateRewards(n);})
            );
        });
        logger.debug("trackAllUserLocationsAndProcess: Futures created: " + futures.size() + ". Getting futures...");
        futures.forEach((n)-> {
            try {
                n.get();
            } catch (InterruptedException | ExecutionException e) {
                logger.error(String.valueOf(e));
            }
        });
        logger.debug("Done!");
    }





    public double getDistance(Location locationA, Location locationB) {
        return Math.sqrt((locationB.longitude - locationA.longitude) * (locationB.longitude - locationA.longitude) + (locationB.latitude - locationA.latitude) * (locationB.latitude - locationA.latitude));

    }

    private void addShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(tracker::stopTracking));
    }




    /**********************************************************************************
     *
     * Methods Below: For Internal Testing
     *
     **********************************************************************************/
    // Database connection will be used for external users, but for testing purposes internal users are provided and stored in memory
    //private final Map<String, User> internalUserMap = new HashMap<>();
    private void initializeInternalUsers() {
        IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
            String userName = "internalUser" + i;
            String phone = "000";
            String email = userName + "@tourGuide.com";
            User user = new User(UUID.randomUUID(), userName, phone, email);
            generateUserLocationHistory(user);

            userService.addUser(user);
        });

        logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
    }

    private void generateUserLocationHistory(User user) {
        IntStream.range(0, 3).forEach(i-> {
            user.addToVisitedLocations(new VisitedLocation(user.getUserId(), new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
        });
    }

    private double generateRandomLongitude() {
        double leftLimit = -180;
        double rightLimit = 180;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    private double generateRandomLatitude() {
        double leftLimit = -85.05112878;
        double rightLimit = 85.05112878;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    private Date getRandomTime() {
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    }

}


