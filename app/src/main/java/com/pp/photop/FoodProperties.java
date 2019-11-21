package com.pp.photop;

public class FoodProperties {
    private String key;
    private String name;
    private String foodImageUrl;
    private String lat, lng, brunch;
    public String glutenfree;
    public String vegan;
    public String pizza;
    public String chinese;
    public String italian;
    public String dessert;
    public String mexican;

    public FoodProperties (String key, String brunch,String glutenfree, String vegan,String pizza,String chinese,String italian,String dessert,String mexican){
        this.key = key;
        this.brunch = brunch;
        this.glutenfree = glutenfree;
        this.vegan = vegan;
        this.pizza = pizza;
        this.chinese = chinese;
        this.italian = italian;
        this.dessert = dessert;
        this.mexican = mexican;
    }
    public String getLat(){return lat;}
    public void setLat(String lat){
        this.lat = lat;
    }
    public String getBrunch(){return brunch;}
    public String getGlutenfreeh(){return glutenfree;}
    public String getVegan(){return vegan;}

    public String getPizza(){return pizza;}

    public String getChinese(){return chinese;}

    public String getItalian(){return italian;}
    public String getDessert(){return dessert;}
    public String getMexican(){return mexican;}

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
