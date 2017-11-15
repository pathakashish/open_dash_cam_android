package com.opendashcam.utils;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;

/**
 * Helper to post messages to worker thread.
 * <p>
 * Created by ashish on 10/5/17.
 */

public final class IoThread {

    private static Handler handler;
    private static HandlerThread thread;

    private IoThread() {
    }

    public static void post(Runnable runnable) {
        checkThreadStarted();
        if (null == runnable) {
            throw new IllegalArgumentException("runnable cannot be null");
        }
        handler.post(runnable);
    }

    public static void postDelayed(Runnable runnable, long delayInMillis) {
        checkThreadStarted();
        if (null == runnable) {
            throw new IllegalArgumentException("runnable cannot be null");
        }
        if (delayInMillis < 0) {
            throw new IllegalArgumentException("delayInMillis cannot be negative value");
        }
        handler.postDelayed(runnable, delayInMillis);
    }

    public static void remove(@NonNull Runnable runnable) {
        checkThreadStarted();
        if (null == runnable) {
            throw new IllegalArgumentException("runnable cannot be null");
        }
        handler.removeCallbacks(runnable);
    }

    public static void removeAllMessages() {
        checkThreadStarted();
        handler.removeCallbacksAndMessages(null);
    }

    private static void checkThreadStarted() {
        if (null == handler) {
            throw new RuntimeException("thread not started yet. Please call IoThred.start() first.");
        }
    }

    public static void start() {
        thread = new HandlerThread("io_thread_open_dash_cam");
        thread.start();
        handler = new Handler(thread.getLooper());
    }

    public static void quit() {
        thread.quit();
        thread = null;
        handler = null;
    }
}
