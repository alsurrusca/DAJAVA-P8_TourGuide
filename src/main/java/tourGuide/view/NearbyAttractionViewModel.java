package tourGuide.view;


import gpsUtil.location.Location;

public class NearbyAttractionViewModel {

    String attractionName;
    Location attractionLocation;
    Location userLocation;
    double distanceInMiles;
    double rewardsPoint;

    public NearbyAttractionViewModel(String attractionName, Location attractionLocation, Location userLocation, double distanceInMiles, double rewardsPoint) {
        this.attractionName = attractionName;
        this.attractionLocation = attractionLocation;
        this.userLocation = userLocation;
        this.distanceInMiles = distanceInMiles;
        this.rewardsPoint = rewardsPoint;
    }

    public NearbyAttractionViewModel() {
    }

    public String getAttractionName() {
        return attractionName;
    }

    public void setAttractionName(String attractionName) {
        this.attractionName = attractionName;
    }

    public Location getAttractionLocation() {
        return attractionLocation;
    }

    public void setAttractionLocation(Location attractionLocation) {
        this.attractionLocation = attractionLocation;
    }

    public Location getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(Location userLocation) {
        this.userLocation = userLocation;
    }

    public double getDistanceInMiles() {
        return distanceInMiles;
    }

    public void setDistanceInMiles(double distanceInMiles) {
        this.distanceInMiles = distanceInMiles;
    }

    public double getRewardsPoint() {
        return rewardsPoint;
    }

    public void setRewardsPoint(double rewardsPoint) {
        this.rewardsPoint = rewardsPoint;
    }
}
