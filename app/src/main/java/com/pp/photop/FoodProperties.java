package com.pp.photop;

public class FoodProperties {
    private String key;
    private String name;
    private String foodImageUrl;
    private String lat, lng, brunch;

    public FoodProperties (String key, String brunch){
        this.key = key;
        this.brunch = brunch;
    }
    public String getLat(){return lat;}
    public void setLat(String lat){
        this.lat = lat;
    }
    public String getBrunch(){return brunch;}
    public void setBrunch(String brunch){
        this.brunch = brunch;
    }
    public String getKey(){
        return key;
    }
    public void setKey(String key) {

        this.key = key;
    }

    public String getFoodImageUrl(){
        return foodImageUrl;
    }
    public void setProfileImageUrl(String profileImageUrl) {

        this.foodImageUrl = profileImageUrl;
    }
}
