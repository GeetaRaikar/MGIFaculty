package com.padmajeet.mgi.techforedu.faculty.util;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.concurrent.TimeUnit;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

public class BootCompleteReceiver extends BroadcastReceiver {
    private static final String uniqueWorkName = "com.padmajeet.eduapp.kiddie.parent.util.NotificaionWorker";
    private static final long repeatIntervalMin = 60;
    private static final long flexIntervalMin = 10;
    public void onReceive(Context context, Intent intent ) {
        if( intent.getAction() == null || !intent.getAction().equals("android.intent.action.BOOT_COMPLETED" ) ) return;

        Constraints constraints = new Constraints.Builder()
                .setRequiresCharging(false)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        PeriodicWorkRequest notificationPeriodicWorkRequest =new PeriodicWorkRequest.Builder(
                NotificationWorker.class, repeatIntervalMin, TimeUnit.MINUTES, flexIntervalMin, TimeUnit.MINUTES
        ).setConstraints(constraints).build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(uniqueWorkName, ExistingPeriodicWorkPolicy.REPLACE, notificationPeriodicWorkRequest );

    }
}