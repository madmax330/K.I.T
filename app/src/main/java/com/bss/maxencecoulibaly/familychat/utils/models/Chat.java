package com.bss.maxencecoulibaly.familychat.utils.models;

import android.view.LayoutInflater;

/**
 * Created by maxencecoulibaly on 2/25/18.
 */

public class Chat {

    private String id;
    private String name;
    private String photoUrl;
    private String user1;
    private String user2;
    private String latestMessage;
    private Long latestActivity;
    private boolean notified;

    public Chat() {
    }

    public Chat(String name, String photoUrl, String user1, String user2, String latestMessage, Long latestActivity, boolean notified) {
        this.name = name;
        this.photoUrl = photoUrl;
        this.user1 = user1;
        this.user2 = user2;
        this.latestMessage = latestMessage;
        this.latestActivity = latestActivity;
        this.notified = notified;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return this.photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getUser1(){
        return this.user1;
    }

    public void setUser1(String user1){
        this.user1 = user1;
    }

    public String getUser2(){
        return this.user2;
    }

    public void setUser2(String user2){
        this.user2 = user2;
    }

    public String getLatestMessage() {
        return this.latestMessage;
    }

    public void setLatestMessage(String latestMessage) {
        this.latestMessage = latestMessage;
    }

    public Long getLatestActivity() {
        return this.latestActivity;
    }

    public void setLatestActivity(Long latestActivity) {
        this.latestActivity = latestActivity;
    }

    public boolean isNotified() {
        return notified;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }

}
