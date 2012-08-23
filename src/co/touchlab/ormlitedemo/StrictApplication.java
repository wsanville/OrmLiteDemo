package co.touchlab.ormlitedemo;

import android.app.Application;
import android.os.Build;
import android.os.StrictMode;

/**
 * Enforces Strict Mode in API level 9 and up.
 */
public class StrictApplication extends Application
{
    @Override
    public void onCreate()
    {
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= 9)
        {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }

        super.onCreate();
    }
}
