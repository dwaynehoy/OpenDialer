package com.squizbit.opendialer.models;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * A SharedPreference wrapper which objectifies a users stored preferences
 */
public class Preferences {
    private SharedPreferences mSharedPreferences;
    private static String PREFERENCE_NAME = "co.strone.stronedialer.preferences";

    /**
     * Creates a new Preferences instance
     * @param context The application or activity context
     */
    public Preferences(Context context){
        mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Retreives the last number that was dialed
     * @return The last number dialed
     */
    public String getLastDialedNumber(){
        return mSharedPreferences.getString("last_dialed_number", "");
    }

    /**
     * Sets the last number dialed
     * @param number The last number dialed
     */
    public void setLastDialedNumber(String number){
        mSharedPreferences.edit()
                .putString("last_dialed_number", number)
                .apply();
    }

    /**
     * Returns a flag indicating that a notification token has been registered for this user
     * @return True if the token has been registered, false otherwise
     */
    public boolean hasRegisteredNotificationToken() {
        return mSharedPreferences.getBoolean("has_notification_token", false);
    }

    /**
     * Sets a flag which determines if a notification token has been registered for this user.
     * @param hasNotificationToken True if the token has been registered, false otherwise
     */
    public void setHasRegisteredNotificationToken(boolean hasNotificationToken){
        mSharedPreferences.edit()
                .putBoolean("has_notification_token", hasNotificationToken)
                .apply();
    }

    /**
     * Clears all the user preferences and internal states. This should be done in case of a user
     * logout
     */
    public void clear(){
        mSharedPreferences.edit()
                .clear()
                .apply();
    }

    /**
     * Adds a missed message to preferences store
     * @param conversationId The conversation id of the missed message
     */
    public void addUnreadMessage(String from, String message, String conversationId){
        int missedMessageCount = mSharedPreferences.getInt("missed_message_count", 0);
        missedMessageCount += 1;

        String missedConversationId = mSharedPreferences.getString("missed_conversation_id", conversationId);

        SharedPreferences.Editor editor = mSharedPreferences.edit();

        if(!conversationId.equals(missedConversationId)){
            editor.putBoolean("missed_message_mixed_conversations", true);
        } else {
            editor.putBoolean("missed_message_mixed_conversations", true);
        }

        editor.putInt("missed_message_count", missedMessageCount);

        editor.apply();
    }

    /**
     * Returns a boolean flag indicating if all missed messages are from different conversations
     * @return True if the messages are from different conversations, false otherwise.
     */
    public boolean areMissedMessagesFromDifferentClientIds(){
        return mSharedPreferences.getBoolean("missed_message_mixed_conversations", false);
    }

    /**
     * Returns the count of missed messages
     * @return The count of missed messages
     */
    public int getUnreadMessageCount(){
        return mSharedPreferences.getInt("missed_message_count", 0);
    }

}
