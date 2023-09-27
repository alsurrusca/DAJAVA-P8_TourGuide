package tourGuide.controller;

import com.jsoniter.output.JsonStream;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tourGuide.DTO.UserPreferencesDTO;
import tourGuide.constant.InternalTest;
import tourGuide.exception.UsernameNotFoundException;
import tourGuide.model.User;
import tourGuide.model.UserPreferences;
import tourGuide.service.TourGuideService;
import tourGuide.service.UserPreferencesService;
import tourGuide.service.UserService;
import tripPricer.Provider;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
public class TourGuideController {

    @Autowired
    TourGuideService tourGuideService;

    @Autowired
    UserService userService;

    @Autowired
    UserPreferencesService userPreferencesService;

    @Autowired
    InternalTest internalTest;

    Logger log = LoggerFactory.getLogger(TourGuideController.class);

    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide !";
    }

    /**
     * Get user location with username
     *
     * @param username - Username of user
     * @return a Json String of a user location
     */
    @RequestMapping("/getLocation")
    public String getLocation(@RequestParam String username) {
        try {
            VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(username));
            return JsonStream.serialize(visitedLocation.location);
        } catch (ExecutionException | InterruptedException e) {
            return ("TourGuide Controller : Error with completable Future : " + e);
        } catch (UsernameNotFoundException e) {
            return "Username not found  : " + e.getMessage();
        }
    }

    /**
     * Get Nearby attraction
     *
     * @param username - Username of user
     * @return Json String of user's closest attractions
     */
    @RequestMapping("/getNearbyAttractions")
    public String getNearbyAttractions(@RequestParam String username) {
        try {
            if (!internalTest.checkIfUserNameExists(username)) {
                log.error(("This username doesn't exist : " + username));
                throw new UsernameNotFoundException(username);
            }
            VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(username));
            return JsonStream.serialize(tourGuideService.getNearByAttractions(visitedLocation));
        } catch (UsernameNotFoundException e) {
            return "Username not found : " + e.getMessage();
        } catch (ArithmeticException e) {
            return ("Unable to get location of : " + username);
        } catch (ExecutionException | InterruptedException e) {
            return ("TourGuide Controller : Error with completable Future : " + e);
        }
    }

    /**
     * Get Rewards by username
     *
     * @param username
     * @return JsonStream - List of userRewards
     */
    @RequestMapping("/getRewards")
    public String getRewards(@RequestParam String username) {
        try {
            if (!internalTest.checkIfUserNameExists(username)) {
                log.error("This username doesn't exist " + username);
                throw new UsernameNotFoundException(username);
            }
            return JsonStream.serialize(tourGuideService.getUserRewards(getUser(username)));
        } catch (UsernameNotFoundException e) {
            log.error("TourGuide Controller : An error occured : " + e.getMessage(), e);
            return "An error occurred:" + e.getMessage();
        }
    }

    /**
     * Get all current location
     *
     * @return Json List with current location
     */
    @RequestMapping("/getAllCurrentLocations")
    public String getAllCurrentLocations() {
        try {
            return JsonStream.serialize(tourGuideService.getAllCurrentLocations());
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get all trip deal
     *
     * @param username
     * @return Json list of trip deals for user
     */
    @RequestMapping("/getTripDeals")
    public String getTripDeals(@RequestParam String username) {
        try {
            if (!internalTest.checkIfUserNameExists(username)) {
                log.error("This username doesn't exist: " + username);
                throw new UsernameNotFoundException(username);
            }

            List<Provider> providers = tourGuideService.getTripDeals(userService.getUsersByUsername(username));
            return JsonStream.serialize(providers);
        } catch (UsernameNotFoundException e) {
            return "Username not found: " + e.getMessage();
        } catch (Exception e) {
            log.error("An error occurred: " + e.getMessage(), e);
            return "An error occurred: " + e.getMessage();
        }
    }


    /**
     * Update user's preferences
     *
     * @param username           - Username of user
     * @param userPreferencesDTO - DTO of user's preferences
     * @return responseEntity
     */
    @PutMapping("/updateUserPreferences}")
    public ResponseEntity<UserPreferences> updateUserPreferences
    (@RequestParam String username, @RequestBody UserPreferencesDTO userPreferencesDTO) {
        UserPreferences userPreferences = userPreferencesService.updateUserPreferences(username, userPreferencesDTO);
        return ResponseEntity.ok(userPreferences);

    }

    private User getUser(String userName) {
        return userService.getUsersByUsername(userName);

    }
}


   

