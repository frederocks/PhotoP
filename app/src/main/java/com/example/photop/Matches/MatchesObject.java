package com.example.photop.Matches;

public class MatchesObject {
    private String userId;
    private String name;
    private String foodImageUrl;
    public MatchesObject (String userId, String name, String foodImageUrl){

        this.userId = userId;
        this.name = name;
        this.foodImageUrl = foodImageUrl;
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
