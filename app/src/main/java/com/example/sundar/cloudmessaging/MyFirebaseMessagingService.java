package com.example.sundar.cloudmessaging;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by sundar on 27/7/16.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Displaying data in log
        //It is optional
        Log.d(TAG, "Message: "+remoteMessage.getData().get("message"));
        Log.d(TAG, "RemoteMessage: "+remoteMessage);
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        String title=remoteMessage.getData().get("title");
        String message= remoteMessage.getData().get("message");
        String image_url=remoteMessage.getData().get("image");
        String url=remoteMessage.getData().get("url");
        boolean isBrowserUrl= Boolean.parseBoolean(remoteMessage.getData().get("browser"));

        Bitmap image=getBitmapFromURL(image_url);
        //Calling method to generate notification
//        sendNotification(remoteMessage.getNotification().getBody());
        sendNotification(title, message, image, url, isBrowserUrl);
    }

    //This method is only generating push notification
    private void sendNotification(String title, String messageBody, Bitmap image, String url, boolean isBrowserUrl) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(messageBody)
//                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setStyle(new NotificationCompat.BigPictureStyle().setSummaryText(messageBody).bigPicture(image));

        if(isBrowserUrl==false) {

            Intent resultIntent = new Intent(this, BrowserActivity.class);
            resultIntent.putExtra("message", messageBody);//   resultIntent.putExtra("image_url", img_URL);
            resultIntent.putExtra("url", url);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);// Adds the back stack
            stackBuilder.addParentStack(BrowserActivity.class);// Adds the Intent to the top of the stack
            stackBuilder.addNextIntent(resultIntent);

            // Gets a PendingIntent containing the entire back stack
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            notificationBuilder.setContentIntent(resultPendingIntent);

        }else if(isBrowserUrl==true){
            // pending implicit intent to view url
            Intent resultIntent = new Intent(Intent.ACTION_VIEW);
            resultIntent.setData(Uri.parse(url));

            PendingIntent pending = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            notificationBuilder.setContentIntent(pending);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }

    public Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
