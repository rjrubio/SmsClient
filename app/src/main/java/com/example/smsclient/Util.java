package com.example.smsclient;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

public class Util {

    public static void scheduleJob(Context context) {
        ComponentName serviceComponent = new ComponentName(context, MyJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        //builder.setMinimumLatency(30 * 1000); // Wait at least 30s
        //builder.setOverrideDeadline(60 * 1000); // Maximum delay 60s
        builder.setPeriodic(3000000 * 1000);

        JobScheduler jobScheduler = (JobScheduler)context.getSystemService(context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());
    }

}
