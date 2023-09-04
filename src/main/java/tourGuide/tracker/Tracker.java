package tourGuide.tracker;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tourGuide.constant.TourGuideConstant;
import tourGuide.service.TourGuideService;
import tourGuide.service.UserService;
import tourGuide.model.User;

/**
 * Tracker - to track user locations and process any rewards
 */
public class Tracker extends Thread {
	private Logger logger = LoggerFactory.getLogger(Tracker.class);
	private final TourGuideService tourGuideService;
	final UserService userService;
	private boolean stop = false;

	public Tracker(TourGuideService tourGuideService, UserService userService) {
		this.tourGuideService = tourGuideService;
		this.userService = userService;
	}


	/**
	 * Assures to shut down the Tracker thread
	 */
	public void stopTracking() {
		stop = true;
	}

	@Override
	public void run() {
		StopWatch stopWatch = new StopWatch();
		while (true) {
			if (Thread.currentThread().isInterrupted() || stop) {
				logger.debug("Tracker stopping");
				break;
			}

			List<User> users = userService.getAllUser();
			logger.debug("Begin Tracker. Tracking " + users.size() + " users.");
			stopWatch.start();
			users.parallelStream().forEach(user -> {
				try {
					tourGuideService.trackUserLocation(user).get();
				} catch (InterruptedException | ExecutionException e) {
					logger.error("Error while tracking user {}", user.getUserName(), e);
				}
			});
			stopWatch.stop();
			logger.debug("Tracker Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
			stopWatch.reset();
			try {
				logger.debug("Tracker sleeping");
				TimeUnit.SECONDS.sleep(TourGuideConstant.TRACKING_POLLING_INTERVAL);
			} catch (InterruptedException e) {
				break;
			}
		}
	}
}
