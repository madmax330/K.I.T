package com.bss.maxencecoulibaly.familychat.utils;

/**
 * Created by maxencecoulibaly on 3/2/18.
 */

public class Constants {

    // Program constants
    public static final String ANONYMOUS = "anonymous";
    public static final int REQUEST_IMAGE = 2;
    public static final int REQUEST_COVER_IMAGE = 4;
    public static final String POST_TOPIC_CONNECTOR = "_0-0_";

    // Database strings
    // SYSTEM STRINGS
    public static final String FAMILIES_CHILD = "families";
    public static final String NOTIFICATIONS_CHILD = "notifications";
    public static final String USERFAMILIES_CHILD = "userfamilies";
    public static final String USER_TOKENS_CHILD = "usertokens";
    public static final String NOTIFICATION_CHAT = "chat";
    public static final String NOTIFICATION_COMMENT = "comment";
    public static final String NOTIFICATION_LIKE = "like";
    public static final String NOTIFICATION_POST = "post";
    public static final String OPENED_FIELD = "opened";

    // Chat strings
    public static final String CHATS_CHILD = "chats";
    public static final String MESSAGES_CHILD = "messages";

    public static final String GROUPCHATS_CHILD = "groupchats";
    public static final String GROUPCHATUSERS_CHILD = "groupchatusers";

    // Profile strings
    public static final String PROFILES_CHILD = "profiles";

    public static final String USER_SIBLING_CHILD = "siblings";

    // Post strings
    public static final String POSTS_CHILD = "posts";
    public static final String POST_COMMENTS_CHILD = "postcomments";
    public static final String POST_LIKES_CHILD = "postlikes";
    public static final String POSTS_GENERAL_CATEGORY = "general";
    public static final String POSTS_TRAVEL_CATEGORY = "travel";
    public static final String POSTS_EVENTS_CATEGORY = "events";
    public static final String USERPOSTS_CHILD = "userposts";
    public static final String USER_LIKES_CHILD = "userlikes";
    public static final String USER_COMMENTS_CHILD = "usercomments";

    // Storage strings
    public static final String STORAGE_FAMILY_PHOTOS_CHILD = "familyphotos";
    public static final String STORAGE_POSTS_CHILD = "posts";
    public static final String STORAGE_PROFILES_CHILD = "profiles";
    public static final String STORAGE_CHATS_CHILD = "chats";

    public static final String STORAGE_PROFILE_PHOTOS_CHILD = "profilephotos";
    public static final String STORAGE_COVER_PHOTOS_CHILD = "coverphotos";
    public static final String STORAGE_GROUP_PHOTOS_CHILD = "groupchatphotos";

    public static final String STORAGE_CHAT_PHOTOS_CHILD = "chatphotos";

    // Intent extras strings
    public static final String EXTRA_USER_ID = "EXTRA_USER_ID";

    public static final String EXTRA_RETURN_SCREEN = "EXTRA_RETURN_SCREEN";

    public static final String EXTRA_CHAT_ID = "EXTRA_CHAT_ID";
    public static final String EXTRA_CHAT_USER_ID = "EXTRA_CHAT_USER_ID";
    public static final String EXTRA_CHAT_NAME = "EXTRA_CHAT_NAME";
    public static final String EXTRA_CHAT_PHOTOURL = "EXTRA_CHAT_PHOTOURL";
    public static final String EXTRA_CHAT_TOAST_MSG = "EXTRA_CHAT_TOAST_MSG";

    public static final String EXTRA_GROUP_CHAT = "EXTRA_GROUP_CHAT";
    public static final String EXTRA_GROUP_PARTICIPANTS = "EXTRA_GROUP_PARTICIPANTS";

    public static final String EXTRA_NEW_STATIC_PROFILE = "EXTRA_NEW_STATIC_PROFILE";
    public static final String EXTRA_LINK_PROFILE_TYPE = "EXTRA_LINK_PROFILE_TYPE";

    public static final String EXTRA_POST_ID = "EXTRA_POST_ID";
    public static final String EXTRA_POST_CATEGORY = "EXTRA_POST_CATEGORY";
    public static final String EXTRA_MY_POSTS = "EXTRA_MY_POSTS";

    public static final String EXTRA_PICK_INTENT = "EXTRA_PICK_INTENT";

    // Shared preferences strings
    public static final String USERS_PREFS = "com.bss.maxencecoulibaly.familychat.USER_PREFS";

    public static final String PREF_USER_TOKEN = "PREF_USER_TOKEN";

    public static final String PREF_USER_ID = "PREF_USER_ID";
    public static final String PREF_USER_PHOTO_URL = "PREF_USER_PHOTO_URL";
    public static final String PREF_USER_NAME = "PREF_USER_NAME";
    public static final String PREF_USER_EMAIL = "PREF_USER_EMAIL";
    public static final String PREF_FAMILY_CODE = "PREF_FAMILY_CODE";
    public static final String PREF_FAMILY_NAME = "PREF_FAMILY_NAME";
    public static final String PREF_FAMILY_PHOTO = "PREF_FAMILY_PHOTO";

    public static final String PREF_FIRST_LOGIN = "PREF_FIRST_LOGIN";

    // Model field strings
    public static final String PHOTO_URL_FIELD = "photoUrl";
    public static final String NAME_FIELD = "name";
    public static final String NOTIFIED_FIELD = "notified";
    public static final String LATEST_MESSAGE_FIELD = "latestMessage";
    public static final String LATEST_ACTIVITY_FIELD = "latestActivity";

    // Notifications strings
    public static final String POST_NOTIFICATION = "posts";
    public static final String COMMENT_NOTIFICATION = "comments";
    public static final String LIKE_NOTIFICATION = "likes";
    public static final String CHAT_NOTIFICATION = "chats";
}
