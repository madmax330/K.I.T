package com.bss.maxencecoulibaly.familychat.utils.models;

/**
 * Created by maxencecoulibaly on 3/5/18.
 */

public class GroupChat extends Chat{

    public GroupChat() {

    }

    public GroupChat(String userId, String name, String photoUrl, String latestMessage, Long latestActivity, boolean notified) {
        setName(name);
        setPhotoUrl(photoUrl);
        setLatestMessage(latestMessage);
        setLatestActivity(latestActivity);
        setUser1(userId);
        setNotified(notified);
    }

}
