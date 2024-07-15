package com.example;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SeriesNotifierScheduler {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void startScheduler() {
        scheduler.scheduleAtFixedRate(SeriesNotifier::checkForNewEpisodes, 0, 2, TimeUnit.MINUTES);
    }
}
