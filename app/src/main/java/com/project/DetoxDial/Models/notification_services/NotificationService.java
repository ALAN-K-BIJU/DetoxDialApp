package com.project.DetoxDial.Models.notification_services;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.SpannableString;
import android.util.Log;

import androidx.core.app.RemoteInput;

import static java.lang.Math.max;

import com.project.DetoxDial.Models.PreferencesManager;

/**
 * This class is taken from opensource free project on github(watomatic) and modified to our use.
 */
public class NotificationService extends NotificationListenerService {
    private final String TAG = NotificationService.class.getSimpleName();
    //private DbUtils dbUtils;

    /**
     * Method that triggers when notification is recived
     * @param sbn
     */
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

      /*  //TEST only
        PreferencesManager prefs = PreferencesManager.getPreferencesInstance(this);
        prefs.setWhatsAppEnabled(true);*/

        if (canReply(sbn) && shouldReply(sbn)) {
            sendReply(sbn);
        }
    }

    private boolean canReply(StatusBarNotification sbn) {
        return isServiceEnabled() &&
                isSupportedPackage(sbn) &&
                NotificationUtils.isNewNotification(sbn) &&
                //isGroupMessageAndReplyAllowed(sbn) &&
                canSendReplyNow(sbn);
    }

    private boolean shouldReply(StatusBarNotification sbn) {
        PreferencesManager prefs = PreferencesManager.getPreferencesInstance(this);
        boolean isGroup = sbn.getNotification().extras.getBoolean("android.isGroupConversation");

        if (isGroup && !prefs.isGroupReplyEnabled()) {
            return false;
        }

//        boolean isContactReplyEnabled = true;
//        //Check contact based replies
//        if (isContactReplyEnabled && !isGroup) {
//            //Title contains sender name (at least on WhatsApp)
//            String senderName = sbn.getNotification().extras.getString("android.title");
//            //Check if should reply to contact
//            boolean isNameSelected =
//                    (ContactsHelper.getInstance(this).hasContactPermission()
//                            && prefs.getReplyToNames().contains(senderName)) ||
//                            prefs.getCustomReplyNames().contains(senderName);
//            if ((isNameSelected && prefs.isContactReplyBlacklistMode()) ||
//                    !isNameSelected && !prefs.isContactReplyBlacklistMode()) {
//                //If contact is on the list and contact reply is on blacklist mode,
//                // or contact is not in the list and reply is on whitelist mode,
//                // we don't want to reply
//                return false;
//            }
//        }

        //Check more conditions on future feature implementations

        //If we got here, all conditions to reply are met
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        //START_STICKY  to order the system to restart your service as soon as possible when it was killed.
        return START_STICKY;
    }

    /**
     * Method that sends the reply
     * @param sbn
     */
    private void sendReply(StatusBarNotification sbn) {
        PreferencesManager prefs = PreferencesManager.getPreferencesInstance(this);
        NotificationWear notificationWear = NotificationUtils.extractWearNotification(sbn);
        // Possibly transient or non-user notification from WhatsApp like
        // "Checking for new messages" or "WhatsApp web is Active"
        if (notificationWear.getRemoteInputs().isEmpty()) {
            return;
        }

        //customRepliesData = CustomRepliesData.getInstance(this);
        String myText = prefs.getAutoReplyText();



        RemoteInput[] remoteInputs = new RemoteInput[notificationWear.getRemoteInputs().size()];

        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle localBundle = new Bundle();//notificationWear.bundle;
        int i = 0;
        for (RemoteInput remoteIn : notificationWear.getRemoteInputs()) {
            remoteInputs[i] = remoteIn;
            // This works. Might need additional parameter to make it for Hangouts? (notification_tag?)
            localBundle.putCharSequence(remoteInputs[i].getResultKey(), myText);
            i++;
        }

        RemoteInput.addResultsToIntent(remoteInputs, localIntent, localBundle);
        try {
            if (notificationWear.getPendingIntent() != null) {
//                if (dbUtils == null) {
//                    dbUtils = new DbUtils(getApplicationContext());
//                }
//                dbUtils.logReply(sbn, NotificationUtils.getTitle(sbn));
                notificationWear.getPendingIntent().send(this, 0, localIntent);
                //if (PreferencesManager.getPreferencesInstance(this).isShowNotificationEnabled()) {
                //NotificationHelper.getInstance(getApplicationContext()).sendNotification(sbn.getNotification().extras.getString("android.title"), sbn.getNotification().extras.getString("android.text"), sbn.getPackageName());
                //}
                cancelNotification(sbn.getKey());
//                if (canPurgeMessages()) {
//                    dbUtils.purgeMessageLogs();
//                    PreferencesManager.getPreferencesInstance(this).setPurgeMessageTime(System.currentTimeMillis());
//                }
            }
        } catch (PendingIntent.CanceledException e) {
            Log.e(TAG, "replyToLastNotification error: " + e.getLocalizedMessage());
        }
    }

    private boolean isSupportedPackage(StatusBarNotification sbn) {
        PreferencesManager prefs = PreferencesManager.getPreferencesInstance(this);
        return prefs.isSupportedAppEnabled(sbn.getPackageName());
    }

    private boolean canSendReplyNow(StatusBarNotification sbn) {
        // Do not reply to consecutive notifications from same person/group that arrive in below time
        // This helps to prevent infinite loops when users on both end uses Watomatic or similar app
        int DELAY_BETWEEN_REPLY_IN_MILLISEC = 10 * 1000;

        String title = NotificationUtils.getTitle(sbn);
        String selfDisplayName = sbn.getNotification().extras.getString("android.selfDisplayName");
        if (title != null && title.equalsIgnoreCase(selfDisplayName)) { //to protect double reply in case where if notification is not dismissed and existing notification is updated with our reply
            return false;
        }
//        if (dbUtils == null) {
//            dbUtils = new DbUtils(getApplicationContext());
//        }
        //long timeDelay = PreferencesManager.getPreferencesInstance(this).getAutoReplyDelay();
        //long timeDelay = 10 * 1000; // 10 seconds
        //return (System.currentTimeMillis() - dbUtils.getLastRepliedTime(sbn.getPackageName(), title) >= max(timeDelay, DELAY_BETWEEN_REPLY_IN_MILLISEC));
        return true;
    }

    private boolean isGroupMessageAndReplyAllowed(StatusBarNotification sbn) {
        String rawTitle = NotificationUtils.getTitleRaw(sbn);
        //android.text returning SpannableString
        SpannableString rawText = SpannableString.valueOf("" + sbn.getNotification().extras.get("android.text"));
        // Detect possible group image message by checking for colon and text starts with camera icon #181
        boolean isPossiblyAnImageGrpMsg = ((rawTitle != null) && (": ".contains(rawTitle) || "@ ".contains(rawTitle)))
                && ((rawText != null) && rawText.toString().contains("\uD83D\uDCF7"));
        if (!sbn.getNotification().extras.getBoolean("android.isGroupConversation")) {
            return !isPossiblyAnImageGrpMsg;
        } else {
            //return PreferencesManager.getPreferencesInstance(this).isGroupReplyEnabled();
            return true;
        }
    }

    private boolean isServiceEnabled() {
        return PreferencesManager.getPreferencesInstance(this).isServiceEnabled();
    }
}
