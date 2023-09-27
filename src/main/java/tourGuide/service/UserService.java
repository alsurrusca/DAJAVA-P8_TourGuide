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

    @Autowired
    private GpsUtil gpsUtil;
    @Autowired
    private RewardsService rewardsService;

    private final Logger logger = LoggerFactory.getLogger(TourGuideService.class);

    public UserService() {
        usersByName = new ConcurrentHashMap<>(TourGuideConstant.CAPACITY);
        this.internalTest = new InternalTest();
        this.gpsUtil = new GpsUtil();
        this.rewardsService = new RewardsService(gpsUtil,new RewardCentral());

    }


    public List<User> getAllUser() {
        log.info("get All user ok");
        return new ArrayList<>(usersByName.values());
    }

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





    /**
     * Add a VisitedLocation to a stored User
     *
     * @param visitedLocation VisitedLocation object to be added to user
     * @param userName name of user
     * @return boolean true if successful, false if user not found
     */
    public boolean addToVisitedLocations(VisitedLocation visitedLocation, String userName) {
        User user = usersByName.get(userName);
        if (user != null) {
            user.addToVisitedLocations(visitedLocation);
            return true;
        }
        logger.debug("addToVisitedLocations: user not found with name " + userName + " returning false");
        return false;
    }

    public int getUserCount() {
        return usersByName.size();
    }



}
