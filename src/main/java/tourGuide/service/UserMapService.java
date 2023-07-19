package tourGuide.service;

import ch.qos.logback.core.util.LocationUtil;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.User;
import tourGuide.tracker.Tracker;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class UserMapService implements UserService {

    // Database connection will be used for external users, but for testing purposes internal users are provided and stored in memory
    private final Map<String, User> internalUserMap = new HashMap<>();
    private Logger log = LoggerFactory.getLogger(UserMapService.class);

    /** Test mode à ajouter
     public final Tracker tracker;
     boolean testMode = true;

     public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {
     this.gpsUtil = gpsUtil;
     this.rewardsService = rewardsService;

     if(testMode) {
     logger.info("TestMode enabled");
     logger.debug("Initializing users");
     initializeInternalUsers();
     logger.debug("Finished initializing users");
     }
     tracker = new Tracker(this);
     addShutDownHook();
     }
*/


     @Override
    public User getUser(String userName) {
        return internalUserMap.get(userName);

    }

    @Override
    public List<User> getAllUser() {
        return internalUserMap.values().stream().collect(Collectors.toList());

    }

    @Override
    public void addUser(User user) {
        if (!internalUserMap.containsKey(user.getUserName())) {
            internalUserMap.put(user.getUserName(), user);
        } else {
            log.error("User with username " + user.getUserName() + " already exist");
        }

    }

/* test ? */

    @Override
    public void initializeInternalUsers() {
        IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
            String userName = "internalUser" + i;
            String phone = "000";
            String email = userName + "@tourGuide.com";
            User user = new User(UUID.randomUUID(), userName, phone, email);
            generateUserLocationHistory(user);

            internalUserMap.put(userName, user);
        });
        log.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
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
