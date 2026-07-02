package com.example.cspf;

import android.app.Application;
import com.example.cspf.utilss.NotificationChannelManager;

public class InsuranceApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        NotificationChannelManager.createNotificationChannels(this);
    }
}