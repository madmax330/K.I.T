package com.bss.maxencecoulibaly.familychat.utils.models;

public class UserPost {

    private String postId;
    private String category;

    public UserPost(){};

    public UserPost(String postId, String category) {
        this.postId = postId;
        this.category = category;
    }

    public String getPostId() {
        return this.postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

}
