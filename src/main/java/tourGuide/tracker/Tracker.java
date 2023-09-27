package tourGuide.tracker;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tourGuide.service.TourGuideService;
import tourGuide.service.UserService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Tracker - to track user locations and process any rewards
 */
public class Tracker extends Thread {
    private final Logger logger = LoggerFactory.getLogger(Tracker.class);
    private static final int trackingPollingIntervalMins = 5;

    private static final long trackingPollingInterval = TimeUnit.MINUTES.toSeconds(20);

    private final TourGuideService tourGuideService;
    final UserService userService = new UserService();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private boolean stop = false;

    public Tracker(TourGuideService tourGuideService) {
        this.tourGuideService = tourGuideService;

        executorService.submit(this);
    }

    /**
     * Assures to shut down the Tracker thread
     */
    public void stopTracking() {
        stop = true;
        executorService.shutdownNow();
    }

    /**
     * Tracker launcher
     */

    @Override
    public void run() {
        StopWatch stopWatch = new StopWatch();

        while (true) {
            if (Thread.currentThread().isInterrupted() || stop) {
                logger.debug("Tracker stopping");
                break;
            }

            logger.debug("Begin Tracker. Tracking " + userService.getUserCount() + " users.");
            stopWatch.start();
            tourGuideService.trackAllUserLocationsAndProcess();
            stopWatch.stop();
            logger.debug("Tracker Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
            stopWatch.reset();
            try {
                logger.debug("Tracker sleeping");
                TimeUnit.SECONDS.sleep(trackingPollingInterval);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}

