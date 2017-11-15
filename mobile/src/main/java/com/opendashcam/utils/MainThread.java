package com.opendashcam.utils;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

/**
 * Helper to post messages to main thread.
 * <p>
 * Created by ashish on 10/5/17.
 */

public final class MainThread {

    private static final Handler handler = new Handler(Looper.getMainLooper());

    private MainThread() {
    }

    public static void post(Runnable runnable) {
        if (null == runnable) {
            throw new IllegalArgumentException("runnable cannot be null");
        }
        handler.post(runnable);
    }

    public static void postDelayed(Runnable runnable, long delayInMillis) {
        if (null == runnable) {
            throw new IllegalArgumentException("runnable cannot be null");
        }
        if (delayInMillis < 0) {
            throw new IllegalArgumentException("delayInMillis cannot be negative value");
        }
        handler.postDelayed(runnable, delayInMillis);
    }

    public static void remove(@NonNull Runnable runnable) {
        if (null == runnable) {
            throw new IllegalArgumentException("runnable cannot be null");
        }
        handler.removeCallbacks(runnable);
    }

    public static void removeAllMessages() {
        handler.removeCallbacksAndMessages(null);
    }
}
