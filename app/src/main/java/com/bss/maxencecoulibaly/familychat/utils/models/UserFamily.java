package com.bss.maxencecoulibaly.familychat.utils.models;

public class UserFamily {

    private String id;
    private String name;
    private String photoUrl;
    private boolean notifications;

    public UserFamily() {}

    public UserFamily(String id, String name, String photoUrl, boolean notifications) {
        this.id = id;
        this.name = name;
        this.photoUrl = photoUrl;
        this.notifications = notifications;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isNotifications() {
        return notifications;
    }

    public void setNotifications(boolean notifications) {
        this.notifications = notifications;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

}
