package tourGuide;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.User;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.service.UserService;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;



public class TestPerformance {
	

    private final UserService userService = new UserService();


    private final TourGuideService tourGuideService = new TourGuideService(new GpsUtil(),new GpsUtil(),new RewardsService(new GpsUtil(),new RewardCentral()),userService);


    private final GpsUtil gpsUtil = new GpsUtil();

    private final RewardsService rewardsService = new RewardsService(gpsUtil,new RewardCentral());
    private StopWatch stopWatch;
    private List<User> allUsers;


    private static final int NUMBER_OF_TEST_USERS = 1000;

    @Before
    public void setUp() {
        InternalTestHelper.setInternalUserNumber(NUMBER_OF_TEST_USERS);
        stopWatch = new StopWatch();
        stopWatch.start();
    }

    @After
    public void tearDown(){
        stopWatch.stop();
        tourGuideService.tracker.stopTracking();
        System.out.println("highVolumeTrackLocation: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");

    }

    /*
     * A note on performance improvements:
     *
     *     The number of users generated for the high volume tests can be easily adjusted via this method:
     *
     *     		InternalTestHelper.setInternalUserNumber(100000);
     *
     *
     *     These tests can be modified to suit new solutions, just as long as the performance metrics
     *     at the end of the tests remains consistent.
     *
     *     These are performance metrics that we are trying to hit:
     *
     *     highVolumeTrackLocation: 100,000 users within 15 minutes:
     *     		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     *
     *     highVolumeGetRewards: 100,000 users within 20 minutes:
     *          assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     */

	

@Test
	public void highVolumeTrackLocation() {
		// Users should be incremented up to 100,000, and test finishes within 15 minutes

        allUsers = userService.getAllUser();

        for(User user : allUsers){
            userService.trackUserLocation(user);
        }

    assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));

}
	

	@Test
	public void highVolumeGetRewards() {

		// Users should be incremented up to 100,000, and test finishes within 20 minutes
        allUsers = userService.getAllUser();
        Attraction attraction = gpsUtil.getAttractions().get(0);

        allUsers.forEach(u->{
            u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date()));
            rewardsService.calculateRewards(u);
        });

        for (User user : allUsers) {
            while (user.getUserRewards().isEmpty()) {
                try {
                    TimeUnit.SECONDS.sleep(10);
                }catch (InterruptedException ex) {
                    System.out.println("Something went wrong in TestPerformance " + ex);
                }
            }
            assertFalse(user.getUserRewards().isEmpty());
        }
        assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}
}
