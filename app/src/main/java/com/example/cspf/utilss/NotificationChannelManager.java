package com.example.cspf.utilss;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.RequiresApi;

public class NotificationChannelManager {
    // 채널 ID
    public static final String CONSULTATION_PROGRESS_CHANNEL = "consultation_progress";

    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createConsultationProgressChannel(context);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void createConsultationProgressChannel(Context context) {
        NotificationChannel channel = new NotificationChannel(
                CONSULTATION_PROGRESS_CHANNEL,
                "상담 진행",
                NotificationManager.IMPORTANCE_HIGH
        );

        channel.setDescription("보험 상담 진행 상태 알림");

        // 알림음 설정
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build();
        channel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, audioAttributes);

        // 진동 설정
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[]{0, 1000, 500, 1000});

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }
}