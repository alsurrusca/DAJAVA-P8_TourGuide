package tourGuide.service;

import org.springframework.stereotype.Service;
import tourGuide.constant.InternalTest;
import tourGuide.model.User;
import tourGuide.model.UserReward;
import tripPricer.Provider;
import tripPricer.TripPricer;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserRewardService {

    private final TripPricer tripPricer= new TripPricer();


    public List<UserReward> getUserRewards(User user) {
        return user.getUserRewards();
    }

    public List<Provider> getTripDeals(User user) {
        int cumulativeRewardPoints = user.getUserRewards().stream().mapToInt(UserReward::getRewardPoints).sum();
        List<Provider> providers = new ArrayList<>();
        while (providers.size() < 10) {
            List<Provider> providerList = tripPricer.getPrice(InternalTest.tripPricerApiKey, user.getUserId(), user.getUserPreferences().getNumberOfAdults(),
                    user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulativeRewardPoints);
            providers.addAll(providerList);
        }
        user.setTripDeals(providers);
        return providers;
    }


}
