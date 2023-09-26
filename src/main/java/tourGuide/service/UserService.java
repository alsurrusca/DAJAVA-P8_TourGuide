package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;
import tourGuide.constant.InternalTest;
import tourGuide.constant.TourGuideConstant;
import tourGuide.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final ConcurrentMap<String, User> usersByName;
    public InternalTest internalTest;
    boolean testMode = true;
    @Autowired
    private GpsUtil gpsUtil;
    @Autowired
    private RewardsService rewardsService;




    private final ExecutorService executorService = Executors.newFixedThreadPool(100);

    private final Logger logger = LoggerFactory.getLogger(TourGuideService.class);

    public UserService() {
        usersByName = new ConcurrentHashMap<>(TourGuideConstant.CAPACITY);
        this.internalTest = new InternalTest();
        this.gpsUtil = new GpsUtil();
        this.rewardsService = new RewardsService(gpsUtil,new RewardCentral());

        if (testMode) {
            logger.info("TestMode enabled");
            logger.debug("Initializing users");
            internalTest.initializeInternalUsers();
            logger.debug("Finished initializing users");
        }

    }


    /**
     * Return list of all user stored
     *
     * @return list of all users
     */
    public List<User> getAllUser() {
        log.info("get All user ok");
        return new ArrayList<>(usersByName.values());
    }

    /**
     * Add user to the collection
     *
     * @param user - User object to be added
     */
    public void addUser(User user) {
        usersByName.put(user.getUsername(), user);
        log.info("Add user ok");
    }

    public User getUsersByUsername(String username) {
        return usersByName.get(username);
    }

    public CompletableFuture<VisitedLocation> trackUserLocation(User user) {
        ExecutorService executorService = Executors.newCachedThreadPool();

        CompletableFuture<VisitedLocation> result = CompletableFuture.supplyAsync(() -> {

            VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());

            user.addToVisitedLocations(visitedLocation);
            rewardsService.calculateRewards(user);
            return visitedLocation;
        }
            , executorService);


        executorService.shutdownNow();
        return result;
    }


    public VisitedLocation getUserLocation(User user) throws InterruptedException, ExecutionException {
        CompletableFuture<VisitedLocation> resultFuture = trackUserLocation(user);

        VisitedLocation result = resultFuture.get();
        assert result != null;

        return (!user.getVisitedLocations().isEmpty()) ?
                user.getLastVisitedLocation() :
                result;

    }



    public void trackListUserLocation(List<User> userList) {
        for (User user : userList) {
            Runnable runnable = () -> trackUserLocation(user);
            executorService.execute(runnable);
        }
    }
}
