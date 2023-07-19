package tourGuide.constant;

import java.util.concurrent.TimeUnit;

public class TourGuideConstant {

    public static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;
    public static final int    ATTRACTION_PROXIMITY_RANGE      = 200;
    public static final int    NUMBER_OF_NEARBY_ATTRACTIONS    = 5;
    public static final int    DEFAULT_PROXIMITY_BUFFER        = 10;
    public static final String TRIP_PRICER_API_KEY             = "test-server-api-key";
    public static final long   TRACKING_POLLING_INTERVAL       = TimeUnit.MINUTES.toSeconds(5);



}
