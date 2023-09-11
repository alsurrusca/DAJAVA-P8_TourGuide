package tourGuide.service;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tourGuide.constant.InternalTestService;
import tourGuide.constant.TourGuideConstant;
import tourGuide.model.User;
import tourGuide.model.UserReward;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
public class UserService {

    private Logger log = LoggerFactory.getLogger(UserService.class);
    private ConcurrentMap<String, User> usersByName;
    public InternalTestService internalTestService;
    boolean testMode = true;

    private User user;
    private Logger logger = LoggerFactory.getLogger(TourGuideService.class);

    public UserService() {
        usersByName = new ConcurrentHashMap<String, User>(TourGuideConstant.CAPACITY);
        this.internalTestService = new InternalTestService();

        if(testMode) {
            logger.info("TestMode enabled");
            logger.debug("Initializing users");
            internalTestService.initializeInternalUsers();
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
        //return new ArrayList<>(internalTestService.internalUserMap.values());

        return usersByName.values().stream().collect(Collectors.toList());
    }

    /**
     * Add user to the collection
     *
     * @param user - User object to be added
     * @return boolean true if ok, false if user already exist
     */
    public User addUser(User user) {
        log.info("Add user ok");

        return usersByName.put(user.getUsername(),user);
    }

    /**
     * Generate and add UserReward
     *
     * @param username
     * @param visitedLocation
     * @param attraction
     * @param rewardPoints
     * @return true -> successfully, false -> user not found
     */
    public void addUserReward(User username, VisitedLocation visitedLocation, Attraction attraction, int rewardPoints) {
        User user = getUsersByUsername(username.getUsername());
        if (user != null) {
            user.addUserReward(new UserReward(visitedLocation, attraction, rewardPoints));
        } else {
            throw new IllegalArgumentException("UserService - UserReward - User with username " + username + " not found");
        }
    }


    public User getUsersByUsername(String username) {
        return usersByName.get(username);
    }



    /**
     * Add a VisitedLocation to a stored User
     *
     * @param visitedLocation - To be added to user
     * @param username
     * @return true if successfully, false if user not found
     */
    public boolean addToVisitedLocation(VisitedLocation visitedLocation, String username) {
        User user = usersByName.get(username);
        if (user != null) {
            user.addToVisitedLocations(visitedLocation);
            return true;
        }
        log.debug("addToVisitedLocations : user " + username + "was not found");
        return false;
    }

}
