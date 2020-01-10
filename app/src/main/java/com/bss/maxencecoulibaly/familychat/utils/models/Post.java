package com.bss.maxencecoulibaly.familychat.utils.models;

/**
 * Created by maxencecoulibaly on 3/3/18.
 */

public class Post {

    private String id;
    private String text;
    private String photoUrl;
    private String thumbnail;
    private String videoUrl;
    private String userId;
    private Long timestamp;
    private String category;

    public Post() {

    }

    public Post(String userId, String text, String photoUrl, String videoUrl, Long timestamp, String category) {
        this.userId = userId;
        this.text = text;
        this.photoUrl = photoUrl;
        this.videoUrl = videoUrl;
        this.timestamp = timestamp;
        this.category = category;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPhotoUrl() {
        return this.photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getVideoUrl() {
        return this.videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public Long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

}
