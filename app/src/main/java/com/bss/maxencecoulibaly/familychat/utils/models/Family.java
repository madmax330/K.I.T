package com.bss.maxencecoulibaly.familychat.utils.models;

public class Family {

    private String code;
    private long dateCreated;
    private String creatorId;
    private String contactEmail;
    private String name;
    private String photoUrl;

    public Family(){}

    public Family(String code, long dateCreated, String creatorId, String contactEmail, String name, String photoUrl) {
        this.code = code;
        this.dateCreated = dateCreated;
        this.creatorId = creatorId;
        this.contactEmail = contactEmail;
        this.name = name;
        this.photoUrl = photoUrl;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public long getDateCreated() {
        return this.dateCreated;
    }

    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getContactEmail() {
        return this.contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
