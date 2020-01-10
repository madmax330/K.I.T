package com.bss.maxencecoulibaly.familychat.utils.models;

/**
 * Created by maxencecoulibaly on 2/27/18.
 */

public class ChatMessage {

    private String id;
    private String userId;
    private String targetId;
    private String name;
    private String message;
    private String photoUrl;
    private String thumbnail;
    private Long timestamp;
    private Boolean read;

    public ChatMessage() {

    }

    public ChatMessage(String userId, String targetId, String name, String message, String photoUrl, Long timestamp, Boolean read){
        this.userId = userId;
        this.targetId = targetId;
        this.name = name;
        this.message = message;
        this.photoUrl = photoUrl;
        this.timestamp = timestamp;
        this.read = read;
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

    public String getTargetId() {
        return this.targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public Long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Boolean getRead() {
        return this.read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }
}
