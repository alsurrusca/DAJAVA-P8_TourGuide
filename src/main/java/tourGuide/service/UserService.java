package tourGuide.service;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tourGuide.Entities.UserLocation;
import tourGuide.constant.TourGuideConstant;
import tourGuide.model.User;
import tourGuide.model.UserReward;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class UserService {

    private Logger log = LoggerFactory.getLogger(UserService.class);
    private ConcurrentMap<String, User> userByName;

    public UserService() {
        userByName = new ConcurrentHashMap<String, User>(TourGuideConstant.CAPACITY);
    }


    /**
     * Return list of all user stored
     *
     * @return list of all users
     */
    public List<User> getAllUser() {
        return userByName.values().stream().collect(Collectors.toList());
    }

    /**
     * Add user to the collection
     *
     * @param user - User object to be added
     * @return boolean true if ok, false if user already exist
     */
    public boolean addUser(User user) {
        if (!userByName.containsKey(user.getUsername())) {
            userByName.put(user.getUsername(), user);
            return true;
        }
        return false;
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
    public boolean addUserReward(String username, VisitedLocation visitedLocation, Attraction attraction, int rewardPoints) {
        User user = getUserByUsername(username);
        if (user != null) {
            user.addUserReward(new UserReward(visitedLocation, attraction, rewardPoints));
            return true;
        }
        log.debug("addUserReward : username " + username + "not found");
        return false;
    }

    public User getUserByUsername(String username) {
        return userByName.get(username);
    }

    public int getUserCount() {
        return userByName.size();
    }

    /**
     * Add a VisitedLocation to a stored User
     *
     * @param visitedLocation - To be added to user
     * @param username
     * @return true if successfully, false if user not found
     */
    public boolean addToVisitedLocation(VisitedLocation visitedLocation, String username) {
        User user = userByName.get(username);
        if (user != null) {
            user.addToVisitedLocations(visitedLocation);
            return true;
        }
        log.debug("addToVisitedLocations : user " + username + "was not found");
        return false;
    }

    public Map<UUID, Location> getAllCurrentLocations() throws ExecutionException, InterruptedException {
        List<User> users = getAllUser();
        Map<UUID, Location> currentLocations = new ConcurrentHashMap<>();
        for (User user : users) {
            currentLocations.put(user.getUserId(),UserLocation.getUserLocation (user).location);
        }
        return currentLocations;
    }


}
