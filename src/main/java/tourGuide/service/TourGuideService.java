package tourGuide.service;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tourGuide.model.User;
import tourGuide.model.UserReward;
import tourGuide.view.NearbyAttractionViewModel;
import tripPricer.Provider;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


public interface TourGuideService {

   List<UserReward> getUserRewards(User user);

   VisitedLocation getUserLocation(User user) throws ExecutionException, InterruptedException;

   List<Provider> getTripDeal(User user);

   List<NearbyAttractionViewModel> getNearAttraction(User user) throws ExecutionException, InterruptedException;

   CompletableFuture<VisitedLocation> trackUserLocation(User user) throws ExecutionException, InterruptedException;

   Map<UUID, Location> getAllCurrentLocations() throws ExecutionException, InterruptedException;


}