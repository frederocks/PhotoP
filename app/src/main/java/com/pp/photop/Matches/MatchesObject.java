package com.pp.photop.Matches;

public class MatchesObject {
    private String userId;
    private String name;
    private String foodImageUrl;
    private String lat, lng;
    private String uploadUserName, phone;

    public MatchesObject (String userId, String name, String foodImageUrl, String lat, String lng, String uploadUserName, String phone){

        this.userId = userId;
        this.name = name;
        this.foodImageUrl = foodImageUrl;
        this.lat = lat;
        this.lng = lng;
        this.uploadUserName = uploadUserName;
        this.phone = phone;

    }
    public String getPhone(){return phone;}
    public String getUploadUserName(){return uploadUserName;}
    public String getLat(){return lat;}
    public void setLat(String lat){
        this.lat = lat;
    }
    public String getLng(){return lng;}
    public void setLng(String lng){
        this.lng = lng;
    }
    public String getUserId(){
        return userId;
    }
    public void setUserId(String userId) {

        this.userId = userId;
    }
    public String getName(){
        return name;
    }
    public void setName(String name) {

        this.name = name;
    }
    public String getFoodImageUrl(){
        return foodImageUrl;
    }
    public void setProfileImageUrl(String profileImageUrl) {

        this.foodImageUrl = profileImageUrl;
    }

}
