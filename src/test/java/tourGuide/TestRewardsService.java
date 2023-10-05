package tourGuide;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.User;
import tourGuide.model.UserReward;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.service.UserRewardService;
import tourGuide.service.UserService;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SpringBootTest(classes = {Application.class})
public class TestRewardsService {

    private final UserService userService = new UserService();

    private final UserRewardService userRewardService = new UserRewardService();


    @Test
    public void getUserRewards() throws ExecutionException, InterruptedException {
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService, userService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(user);
        Attraction attraction = gpsUtil.getAttractions().get(0);
        UserReward userReward = new UserReward(visitedLocation, attraction, 10);
        user.addUserReward(userReward);
        List<UserReward> rewards = userRewardService.getUserRewards(user);

        assertFalse(rewards.isEmpty());
    }

    @Test
    public void isWithinAttractionProximity() {
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        Attraction attraction = gpsUtil.getAttractions().get(0);
        assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
    }


    @Test
    public void nearAllAttractions() throws ExecutionException, InterruptedException {
        RewardsService rewardsService = new RewardsService(new GpsUtil(), new RewardCentral());
        rewardsService.setProximityBuffer(Integer.MAX_VALUE);
        GpsUtil gpsUtil = new GpsUtil();
        InternalTestHelper.setInternalUserNumber(1);
        TourGuideService tourGuideService = new TourGuideService(new GpsUtil(), rewardsService, userService);
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(user);
        Attraction attraction = gpsUtil.getAttractions().get(0);
        UserReward userReward = new UserReward(visitedLocation, attraction, 10);
        user.addUserReward(userReward);
        rewardsService.calculateRewards(user);
        while (user.getUserRewards().isEmpty()) {
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException ex) {
                System.out.println("Something went wrong: " + ex);
            }

            assertFalse(user.getUserRewards().isEmpty());
        }

    }
}


