package com.padmajeet.mgi.techforedu.faculty.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.padmajeet.mgi.techforedu.faculty.ActivitySplashScreen;
import com.padmajeet.mgi.techforedu.faculty.R;
import com.padmajeet.mgi.techforedu.faculty.model.Notification;
import com.padmajeet.mgi.techforedu.faculty.model.Staff;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class NotificationWorker extends Worker {


    private static Context context;
    private Gson gson;
    private Staff loggedInUser;
    private List<Notification> notificationList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference notificationColRef = db.collection("Notification");

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        gson = Utility.getGson();
        SessionManager sessionManager = new SessionManager(getApplicationContext());
        String userJson = sessionManager.getString("loggedInUserForWorker");
        loggedInUser = gson.fromJson(userJson, Staff.class);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (loggedInUser == null) {
            gson = Utility.getGson();
            SessionManager sessionManager = new SessionManager(getApplicationContext());
            String userJson = sessionManager.getString("loggedInUserForWorker");
            loggedInUser = gson.fromJson(userJson, Staff.class);
        }
        if (loggedInUser != null) {
            getParentNotifications();
        }
        return Result.success();
    }

    private void getParentNotifications() {
        notificationList.clear();
        notificationColRef.whereEqualTo("recipientId", loggedInUser.getId())
                .whereEqualTo("recipientType", "P")
                .whereEqualTo("status","O")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(QueryDocumentSnapshot queryDocumentSnapshot:queryDocumentSnapshots){
                            Notification notification = queryDocumentSnapshot.toObject(Notification.class);
                            notification.setId(queryDocumentSnapshot.getId());
                            notificationList.add(notification);
                        }
                        if(notificationList!=null&& notificationList.size()>0) {
                            for (int i=0;i<notificationList.size();i++) {
                                Notification notification = notificationList.get(i);
                                addNotification(i,notification.getSubject(),notification.getDescription());
                                notificationList.get(i).setStatus("N");
                            }
                            updateNotificationList();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private String CHANNEL_ID = "MGI";

    private void createNotificationChannel() {
        CharSequence channelName = CHANNEL_ID;
        String channelDesc = "channelDesc";
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
            channel.setDescription(channelDesc);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void addNotification(int id, String title, String message) {
        createNotificationChannel();
        Intent intent = new Intent(getApplicationContext(), ActivitySplashScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.home_icon)
                        .setContentTitle(title)   //this is the title of notification
                        .setColor(101)
                        .setContentText(message)   //this is the message showed in notification
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setContentIntent(contentIntent)
                        //.setAutoCancel(true)
                        //.setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setChannelId(CHANNEL_ID);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        builder.setSound(uri);

        // Add as notification
        // NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.notify(id, builder.build());
    }


    private void updateNotificationList() {
        for(Notification notification:notificationList){
            notificationColRef.document(notification.getId()).set(notification).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                }
            });
        }
    }

}
