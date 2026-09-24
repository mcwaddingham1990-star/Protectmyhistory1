package com.stuffapp.protectmyhistory;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;

public final class ForegroundAppResolver {
    private ForegroundAppResolver() {}
    public static String recentApp(Context context) {
        try {
            UsageStatsManager manager=(UsageStatsManager)context.getSystemService(Context.USAGE_STATS_SERVICE);
            if(manager==null)return "Unknown app";
            long now=System.currentTimeMillis(); UsageEvents events=manager.queryEvents(now-120000,now+1000);
            UsageEvents.Event event=new UsageEvents.Event(); String latest=null; long when=-1;
            while(events!=null&&events.hasNextEvent()){events.getNextEvent(event);int type=event.getEventType();if((type==UsageEvents.Event.ACTIVITY_RESUMED||type==UsageEvents.Event.MOVE_TO_FOREGROUND)&&event.getTimeStamp()>when){latest=event.getPackageName();when=event.getTimeStamp();}}
            if(latest==null||latest.equals(context.getPackageName()))return "Unknown app";
            if(latest.startsWith("com.android.chrome")||latest.startsWith("com.chrome."))return "Chrome";
            if(latest.startsWith("com.microsoft.emmx"))return "Microsoft Edge";
            return latest;
        }catch(Throwable t){return "Unknown app";}
    }
}
