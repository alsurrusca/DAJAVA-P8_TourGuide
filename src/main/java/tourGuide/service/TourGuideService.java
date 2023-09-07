package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.User;
import tourGuide.model.UserReward;
import tripPricer.Provider;
import tripPricer.TripPricer;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

@Service
public class TourGuideService {
    private final GpsUtil gpsUtil;
    private final TripPricer tripPricer = new TripPricer();
    private final RewardsService rewardsService;
    public UserService userService;
    private TripService tripService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(100);
    private Logger logger = LoggerFactory.getLogger(TourGuideService.class);

    public TrackerService tracker;


    public TourGuideService(GpsUtil gpsUtil, GpsUtil gpsUtil1, RewardsService rewardsService, UserService userService) {
        this.gpsUtil = gpsUtil1;
        this.rewardsService = rewardsService;
        this.userService = userService;
        this.tracker = new TrackerService(TourGuideService.this, userService);
    }


    public List<UserReward> getUserRewards(User user) {
        return user.getUserRewards();
    }


    public VisitedLocation getUserLocation(UUID userId) {
        return gpsUtil.getUserLocation(userId);
    }


    public VisitedLocation trackUserLocation(User user) {
        VisitedLocation visitedLocation = new VisitedLocation(user.getUserId(), new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime());
        user.getVisitedLocations().add(visitedLocation);
        rewardsService.calculateRewards(user);
        return new VisitedLocation(user.getUserId(), new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime());
    }


    public List<Provider> getTripDeals(User user) {
        int cumulativeRewardPoints = user.getUserRewards().stream().mapToInt(UserReward::getRewardPoints).sum();
        List<Provider> providers = new ArrayList<>();
        while (providers.size()<10){
            List<Provider> providerList =  tripPricer.getPrice(tripPricerApiKey, user.getUserId(), user.getUserPreferences().getNumberOfAdults(),
                    user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulativeRewardPoints);
           providers.addAll(providerList);
        }
        user.setTripDeals(providers);
        return providers;
    }

    public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation) {

        Map<Double, Attraction> attractionMap = new HashMap<>();
         gpsUtil.getAttractions().forEach((n) -> {
             attractionMap.put(getDistance(n,visitedLocation.location), n);
         });

         TreeMap<Double, Attraction> sortedAttraction = new TreeMap<>(attractionMap);
         return new ArrayList<>(sortedAttraction.values()).subList(0,5);

         }





    public double getDistance(Location locationA, Location locationB) {
        return Math.sqrt((locationB.longitude - locationA.longitude) * (locationB.longitude - locationA.longitude) + (locationB.latitude - locationA.latitude) * (locationB.latitude - locationA.latitude));

    }

    public Map<UUID, Location> getAllCurrentLocations() throws ExecutionException, InterruptedException {
        List<User> users = userService.getAllUser();
        Map<UUID, Location> currentLocations = new ConcurrentHashMap<>();
        for (User user : users) {
            currentLocations.put(user.getUserId(), getUserLocation(user.getUserId()).location);
        }
        return currentLocations;
    }

    public void addUser(User user) {
        userService.addUser(user);
    }

    public List<User> getAllUsers() {
        return userService.getAllUser();
    }

    /**********************************************************************************
     *
     * Methods Below: For Internal Testing
     *
     **********************************************************************************/
    public static final String tripPricerApiKey = "test-server-api-key";
    // Database connection will be used for external users, but for testing purposes internal users are provided and stored in memory
    private final Map<String, User> internalUserMap = new HashMap<>();

    private void initializeInternalUsers() {
        IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
            String userName = "internalUser" + i;
            String phone = "000";
            String email = userName + "@tourGuide.com";
            User user = new User(UUID.randomUUID(), userName, phone, email);
            generateUserLocationHistory(user);

            internalUserMap.put(userName, user);
        });
        logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
    }

    private void generateUserLocationHistory(User user) {
        IntStream.range(0, 3).forEach(i -> {
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
