package com.pp.photop.Matches;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;;

public class MatchesObject {
    private String userId;
    private String name;
    private String uploadUri;
    private String lat;
    private String lng;
    private String uploadUserName;
    private String phone;

    public MatchesObject() {
    }

    public MatchesObject(String name, String uploadUri, String lat, String lng, String uploadUserName, String phone) {
        this.userId = null;
        this.name = name;
        this.uploadUri = uploadUri;
        this.lat = lat;
        this.lng = lng;
        this.uploadUserName = uploadUserName;
        this.phone = phone;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUploadUri() {
        return uploadUri;
    }

    public void setUploadUri(String uploadUri) {
        this.uploadUri = uploadUri;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getUploadUserName() {
        return uploadUserName;
    }

    public void setUploadUserName(String uploadUserName) {
        this.uploadUserName = uploadUserName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setDefaultsWhenNull(String name, String uploadUri, String lat, String lng, String uploadUserName, String phone) {
        //convenience method to set default values if our current value is null
        if (this.name == null) {
            this.name = name;
        }
        if (this.uploadUri == null) {
            this.uploadUri = uploadUri;
        }
        if (this.lat == null) {
            this.lat = lat;
        }
        if (this.lng == null) {
            this.lng = lng;
        }
        if (this.uploadUserName == null) {
            this.uploadUserName = uploadUserName;
        }
        if (this.phone == null) {
            this.phone = phone;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "userId: " + getNullableString(userId) +
                "; name: " + getNullableString(name) +
                "; uploadUri: " + getNullableString(uploadUri) +
                "; lat: " + getNullableString(lat) +
                "; lng: " + getNullableString(lng) +
                "; uploadUserName: " + getNullableString(uploadUserName) +
                "; phone: " + getNullableString(phone);
    }

    @NonNull
    private String getNullableString(@Nullable String string) {
        return string != null ? string : "(NULL)";
    }
}
