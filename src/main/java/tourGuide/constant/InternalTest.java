package tourGuide.constant;

import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.User;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static java.util.stream.IntStream.range;

@Service
public class InternalTest {
    private final Logger log = LoggerFactory.getLogger(InternalTest.class);

    /**********************************************************************************
     * Methods Below: For Internal Testing
     *
     **********************************************************************************/
    public static final String tripPricerApiKey = "test-server-api-key";
    // Database connection will be used for external users, but for testing purposes internal users are provided and stored in memory
    public final Map<String, User> internalUserMap = new HashMap<>();


    public void initializeInternalUsers() {
        range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
            String userName = "internalUser" + i;
            String phone = "000";
            String email = userName + "@tourGuide.com";
            User user = new User(UUID.randomUUID(), userName, phone, email);
            generateUserLocationHistory(user);

            addUser(user);
            internalUserMap.put(userName, user);
        });
        log.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
    }

    /**
     * Add a user to the InternalUserMap if it does not contain already the userName
     * @param user - User
     */
    public void addUser(User user) {
        if (!internalUserMap.containsKey(user.getUsername())) {
            internalUserMap.put(user.getUsername(), user);
        } else {
            log.error(user.getUsername() + " already exist ! ");
        }
    }

    /**
     * Generate a user location history of 3 visited locations for the current user
     * @param user - User
     */
    public void generateUserLocationHistory(User user) {
        range(0, 3).forEach(i-> user.addToVisitedLocations(new VisitedLocation(user.getUserId(), new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime())));
    }

    /**
     * Generate a random Longitude
     * @return double of longitude
     */
    public double generateRandomLongitude() {
        double leftLimit = -180;
        double rightLimit = 180;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }
    /**
     * Generate a random latitude
     * @return double of latitude
     */
    public double generateRandomLatitude() {
        double leftLimit = -85.05112878;
        double rightLimit = 85.05112878;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    /**
     * Generate a random LocalDateTime with java.time, in UTC time
     * @return Date of a random time
     */
    public Date getRandomTime() {
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    }

    /**
     * Check if the InternalUserMap contains already the userName
     *
     * @param userName the string of the username
     * @return true if it's ok
     */
    public boolean checkIfUserNameExists(String userName) {
        return internalUserMap.containsKey(userName) ? true : false;
    }



}
