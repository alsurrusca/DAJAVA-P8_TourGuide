package tourGuide.controller;

import com.jsoniter.output.JsonStream;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tourGuide.DTO.UserPreferencesDTO;
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

    Logger log = LoggerFactory.getLogger(TourGuideController.class);

    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide !";
    }

    @RequestMapping("/getLocation")
    public String getLocation(@RequestParam String userName)  {
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName).getUserId());
        return JsonStream.serialize(visitedLocation.location);
    }

    //  TODO: Change this method to no longer return a List of Attractions.
    //  Instead: Get the closest five tourist attractions to the user - no matter how far away they are.
    //  Return a new JSON object that contains:
    // Name of Tourist attraction,
    // Tourist attractions lat/long,
    // The user's location lat/long,
    // The distance in miles between the user's location and each of the attractions.
    // The reward points for visiting each Attraction.
    //    Note: Attraction reward points can be gathered from RewardsCentral
    @RequestMapping("/getNearbyAttractions")
    public String getNearbyAttractions(@RequestParam String userName) throws ExecutionException, InterruptedException {
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName).getUserId());
        if (visitedLocation == null) {
            return JsonStream.serialize("User" + userName + "not found");
        }
        return JsonStream.serialize(tourGuideService.getNearByAttractions(visitedLocation));
    }


    @RequestMapping("/getRewards")
    public String getRewards(@RequestParam String userName) {
        return JsonStream.serialize(tourGuideService.getUserRewards(getUser(userName)));
    }

    // TODO: Get a list of every user's most recent location as JSON
    //- Note: does not use gpsUtil to query for their current location,
    //        but rather gathers the user's current location from their stored location history.
    //
    // Return object should be the just a JSON mapping of userId to Locations similar to:
    //     {
    //        "019b04a9-067a-4c76-8817-ee75088c3822": {"longitude":-48.188821,"latitude":74.84371}
    //        ...
    //     }

    @RequestMapping("/getAllCurrentLocations")
    public String getAllCurrentLocations() throws ExecutionException, InterruptedException {

        return JsonStream.serialize(tourGuideService.getAllCurrentLocations());
    }

    @RequestMapping("/getTripDeals")
    public String getTripDeals(@RequestParam String username) {
        List<Provider> providers = tourGuideService.getTripDeals(userService.getUsersByUsername(username));
        return JsonStream.serialize(providers);
    }



    /**
     * Update user's preferences
     * @param username
     * @param userPreferencesDTO
     * @return responseEntity
     */
    @PutMapping("/updateUserPreferences}")
    public ResponseEntity<UserPreferences> updateUserPreferences
            (@RequestParam String username, @RequestBody UserPreferencesDTO userPreferencesDTO) {
        UserPreferences userPreferences = userPreferencesService.userUpdatePreferences(username, userPreferencesDTO);
        return ResponseEntity.ok(userPreferences);

    }

    private User getUser(String userName) {
        return userService.getUsersByUsername(userName);

    }
}


   

