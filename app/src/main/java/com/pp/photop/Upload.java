package com.pp.photop;

class Upload {
    public String name;
    public String uploadUserName;
    public String phone;
    public String userId;
    public String uploadUri;
    public String glutenfree;
    public String vegan;
    public String pizza;
    public String chinese;
    public String italian;
    public String dessert;
    public String brunch;
    public String mexican;
    public String lat;
    public String lng;
    public Float rating;
    public Long yes;
    public Long no;

    public Upload() {
        this(null, null, null, false, false, false, false, false, false, false, false, null, null, (float)1.0, null, null, 1L, 1L);
    }

    public Upload(String name, String userId, String uploadUri, boolean glutenfree, boolean vegan, boolean pizza, boolean chinese, boolean italian,
                  boolean dessert, boolean brunch, boolean mexican, String lat, String lng, Float rating, String uploadUserName, String phone, Long yes, Long no) {
        this.name = name;
        this.userId = userId;
        this.uploadUri = uploadUri;
        this.glutenfree = String.valueOf(glutenfree);
        this.vegan = String.valueOf(vegan);
        this.pizza = String.valueOf(pizza);
        this.chinese = String.valueOf(chinese);
        this.italian = String.valueOf(italian);
        this.dessert = String.valueOf(dessert);
        this.brunch = String.valueOf(brunch);
        this.mexican = String.valueOf(mexican);
        this.lat = lat;
        this.lng = lng;
        this.rating = rating;
        this.uploadUserName = uploadUserName;
        this.phone = phone;
        this.yes = yes;
        this.no = no;
    }
}
