package com.bss.maxencecoulibaly.familychat.utils.models;

public class PostLike {

    private String id;
    private String userId;
    private String postId;
    private String postCategory;
    private String postUserId;
    private Long timestamp;

    public PostLike() {

    }

    public PostLike(String userId, String postId, String postCategory, String postUserId, Long timestamp) {
        this.userId = userId;
        this.postId = postId;
        this.postCategory = postCategory;
        this.postUserId = postUserId;
        this.timestamp = timestamp;
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

    public String getPostId() {
        return this.postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getPostCategory() {
        return postCategory;
    }

    public void setPostCategory(String postCategory) {
        this.postCategory = postCategory;
    }

    public void setPostUserId(String postUserId) {
        this.postUserId = postUserId;
    }

    public String getPostUserId() {
        return this.postUserId;
    }

    public Long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

}
