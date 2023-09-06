package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.javamoney.moneta.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tourGuide.constant.TourGuideConstant;
import tourGuide.model.User;
import tourGuide.model.UserReward;
import tourGuide.view.NearbyAttractionViewModel;
import tripPricer.Provider;
import tripPricer.TripPricer;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class TourGuideService {
    private final GpsUtil gpsUtil;
    private final TripPricer tripPricer = new TripPricer();
    private final RewardsService rewardsService;
    private final UserService userService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(100);
    private Logger logger = LoggerFactory.getLogger(TourGuideService.class);


    public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService, UserService userService) {
        this.gpsUtil = gpsUtil;
        this.rewardsService = rewardsService;
        this.userService = userService;
    }




    public List<UserReward> getUserRewards(User user) {
        return user.getUserRewards();
    }


    public VisitedLocation getUserLocation(User user) throws ExecutionException, InterruptedException {
        return (user.getVisitedLocations().isEmpty()) ?
                trackUserLocation(user).get() : user.getLastVisitedLocation();
    }

    public CompletableFuture<VisitedLocation> trackUserLocation(User user) {
        logger.debug("Tracking user " + user.getUsername());

        return CompletableFuture.supplyAsync(() -> {
            VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
            user.addToVisitedLocations(visitedLocation);
            rewardsService.calculateRewards(user);
            return visitedLocation;
        }, executorService);
    }

    public List<Provider> getTripDeal(User user) {
        int cumulativeRewardPoints = user.getUserRewards().stream().mapToInt(UserReward::getRewardPoints).sum();
        // get list of providers based on user preferences regarding nb of adults, nb of children and trip duration
        List<Provider> providers = tripPricer.getPrice(TourGuideConstant.TRIP_PRICER_API_KEY, user.getUserId(),
                user.getUserPreferences().getNumberOfAdults(),
                user.getUserPreferences().getNumberOfChildren(),
                user.getUserPreferences().getTripDuration(),
                cumulativeRewardPoints);
        // find trip deals within user's price range
        Money userLowerPricePoint = user.getUserPreferences().getLowerPricePoint();
        Money userHigherPricePoint = user.getUserPreferences().getHighPricePoint();
        List<Provider> providersWithinUsersPriceRange = providers
                .stream()
                .filter(provider -> Money.of(provider.price, user.getUserPreferences().getCurrency())
                        .isGreaterThanOrEqualTo(userLowerPricePoint)
                        && Money.of(provider.price, user.getUserPreferences().getCurrency())
                        .isLessThanOrEqualTo(userHigherPricePoint))
                .collect(Collectors.toList());
        user.setTripDeals(providersWithinUsersPriceRange);
        return providersWithinUsersPriceRange;
    }


    public List<NearbyAttractionViewModel> getNearAttraction(User user)
            throws ExecutionException, InterruptedException {
        VisitedLocation lastVisitedLocation = getUserLocation(user);

        return gpsUtil.getAttractions()
                .stream()
                .sorted(Comparator.comparingDouble(attraction -> rewardsService.getDistance(lastVisitedLocation.location, attraction)))
                .limit(TourGuideConstant.NUMBER_OF_NEARBY_ATTRACTIONS)
                .map(attraction -> new NearbyAttractionViewModel(
                        attraction.attractionName,
                        attraction,
                        lastVisitedLocation.location,
                        rewardsService.getDistance(lastVisitedLocation.location, attraction),
                        rewardsService.getRewardPoints(attraction, user)
                ))
                .collect(Collectors.toList());
    }

    public Map<UUID, Location> getAllCurrentLocations() throws ExecutionException, InterruptedException {
        List<User> users = userService.getAllUser();
        Map<UUID, Location> currentLocations = new ConcurrentHashMap<>();
        for (User user : users) {
            currentLocations.put(user.getUserId(), getUserLocation(user).location);
        }
        return currentLocations;
    }


}
